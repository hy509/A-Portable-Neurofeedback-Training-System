from config import cfg
from tensorflow.keras.callbacks import (ReduceLROnPlateau)
from cnn_model2 import *
import matplotlib.pyplot as plt
from data_set_creat import *
import datetime
import tensorflow as tf
import numpy as np
from sklearn.model_selection import KFold



#Configure GPU to limit GPU memory growth
def GPU_Config():
    gpus = tf.config.experimental.list_physical_devices('GPU')
    if gpus:
        try:
            # Currently, memory growth needs to be the same across GPUs
            for gpu in gpus:
                tf.config.experimental.set_memory_growth(gpu, True)
            logical_gpus = tf.config.experimental.list_logical_devices('GPU')
            print(len(gpus), "Physical GPUs,", len(logical_gpus),
                  "Logical GPUs")
        except RuntimeError as e:
            # Memory growth must be set before GPUs have been initialized
            print(e)

def _parse_example(example_string):
    """
    Tfrecod data parsing
    :param example_string:
    :return:
    """

    feature_description = {
        'data': tf.io.FixedLenFeature([512 * 6], tf.float32),
        'label': tf.io.FixedLenFeature([1], tf.int64)
    }
    feature_dict = tf.io.parse_example(example_string,
                                       feature_description)
    data = tf.reshape(feature_dict['data'], [512*3, 6, 1])

    labels = feature_dict['label']
    labels = tf.cast(labels, tf.int64)

    return data, labels


def gen_data_batch(file_pattern, batch_size, num_repeat=100, is_training=True):
    files = tf.data.Dataset.list_files(file_pattern)
#    if is_training:
    dataset = files.flat_map(tf.data.TFRecordDataset)
        #dataset = files.interleave(map_func=tf.data.TFRecordDataset,cycle_length=1) 
         # cycle_length: interleaved file read parallelism
    #dataset = dataset.repeat(num_repeat)
    dataset = dataset.map(_parse_example, num_parallel_calls=4)
    dataset = dataset.shuffle(buffer_size=10)
    dataset = dataset.batch(batch_size)
    dataset = dataset.prefetch(buffer_size=tf.data.AUTOTUNE)
    '''
    else:
        dataset = files.flat_map(tf.data.TFRecordDataset)
        dataset = dataset.repeat(num_repeat)
        dataset = dataset.map(_parse_example, num_parallel_calls=4)
        dataset = dataset.batch(batch_size)
        dataset = dataset.prefetch(buffer_size=tf.data.AUTOTUNE)
    '''
    return dataset








def train_and_val():
    model = bp_model()

    # Adam Optimizer
    optimizer = tf.keras.optimizers.Adam(learning_rate=1e-4)
    # Cross-Entropy Loss Function
    loss = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)
    # Configuring Optimizer, Loss Function, and Evaluation Metrics for Training
    #model.compile(optimizer=optimizer, loss=loss, metrics=['accuracy'])

    #Printing Neural Network Architecture and Counting Parameters
    #model.summary()

    
    tfrecord=""
    dataset = gen_data_batch(tfrecord,
                                   cfg.batch_size,
                                   is_training=True)
    data, label = next(iter(dataset))    
    data=np.array(data)
    label=np.array(label)



                               
    # Configuring Callback Functions
    #callback = [
    #    ReduceLROnPlateau(monitor='loss', factor=0.5, patience=5, verbose=1),
    #    tf.keras.callbacks.ModelCheckpoint('./model/zhuanzhuli_classify.h5', monitor='val_accuracy', verbose=1, save_best_only=True, save_weights_only=False, mode='auto', period=1)
    #]


    # Model Training
    kfold = KFold(n_splits=10, shuffle=True)
    fold_no = 0
    val_acc_t=[]
    val_loss_t=[]
    '''
    print(type(data))
    print(type(label))
    plt.figure()
    plt.subplot(1, 2, 1)
    plt.title('Validation Accuracy')

    plt.subplot(1, 2, 2)
    plt.title('Validation Loss')
    '''
    for train_index, test_index in kfold.split(data):
        fold_no+=1
        model = al_model()
        optimizer = tf.keras.optimizers.Adam(learning_rate=1e-4)
        # Cross-Entropy Loss Function
        loss = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True)
        model.compile(optimizer=optimizer, loss=loss, metrics=['accuracy'])

        train_X, train_y = data[train_index], label[train_index]
        test_X, test_y = data[test_index], label[test_index]

        history = model.fit(train_X, train_y,
                            # batch_size=3,
                            epochs=10,
                            #validation_split=0.125,
                            validation_data=[test_X, test_y],
                            batch_size = 32,
                            #validation_split=0.05,
                            #callbacks=callback,
                            shuffle=True,
                            verbose=1,
                            #steps_per_epoch=10,
                            #validation_steps=None,
                            validation_freq=1)
        val_acc = history.history['val_accuracy']
        val_loss = history.history['val_loss']
        val_acc_t.append(val_acc)
        val_loss_t.append(val_loss)
    #    print(type(val_acc))
        '''
        plt.subplot(1, 2, 1)
        plt.plot(range(1,6), val_acc, label="fold "+str(fold_no))    
        plt.legend(loc='lower right')

        plt.subplot(1, 2, 2)
        plt.plot(range(1,6), val_loss, label="fold "+str(fold_no))
        plt.legend(loc='upper right')
        '''
    #result
#    pd.DataFrame(history.history).to_csv('training_log.csv', index=True)
    print("end")
    df_acc = pd.DataFrame(val_acc_t)
    df_acc.to_excel('')

    df_loss = pd.DataFrame(val_loss_t)
    df_loss.to_excel('')
    '''
    acc = history.history['accuracy']
    val_acc = history.history['val_accuracy']
    loss = history.history['loss']
    val_loss = history.history['val_loss']

    # Plotting Training and Testing Accuracy/Loss Curves
    epochs_range = range(cfg.epochs)
    plt.figure()
    plt.subplot(1, 2, 1)
    plt.plot(epochs_range, acc, label='Training Accuracy')
    plt.plot(epochs_range, val_acc, label='Validation Accuracy')
    plt.legend(loc='lower right')
    plt.title('Training and Validation Accuracy')

    plt.subplot(1, 2, 2)
    plt.plot(epochs_range, loss, label='Training Loss')
    plt.plot(epochs_range, val_loss, label='Validation Loss')
    plt.legend(loc='upper right')
    plt.title('Training and Validation Loss')

    # Save the plot
    result_png = "./result_img/" + "result_" + datetime.datetime.now(
    ).strftime("%m%d_%H%M%S") + ".png"
    plt.savefig(result_png)
    plt.show()


    # Save the Model
    # model_path = "./model/posture_classify.h5"
    # model.save(model_path)
    '''






if __name__ == '__main__':
    train_and_val()


