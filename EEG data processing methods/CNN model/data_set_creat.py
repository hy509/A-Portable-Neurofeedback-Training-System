import pandas as pd
import numpy as np
import tensorflow as tf
import random

'''
for i in range(shards):
    writer = tf.data.experimental.TFRecordWriter(f"output_file-part-{i}.tfrecord")
    writer.write(raw_dataset.shard(shards, i))
'''

def _float_feature(value):
    """Returns a float_list from a float / double."""
    return tf.train.Feature(
        float_list=tf.train.FloatList(value=value))  

def _int64_feature(value):
    """Returns an int64_list from a bool / enum / int / uint."""
    return tf.train.Feature(int64_list=tf.train.Int64List(value=[value]))

def open_excel(filename):
    """
    Open the dataset and proceed with data processing
    :param filename: file name
    :return: Feature set data, label set data
    """
    num=filename
    readbook = pd.read_excel(f'./data_eeg_raw_data/{filename}.xlsx', engine='openpyxl',header = None)
    target = int(num)%3
    return readbook, target


def gennerate_terecord_file(tfrecordfilename,start,end):
    """
    Generate tfrecord file
    :param tfrecordfilename: The name of the generated tfrecord file
    :return:
    """
    image_size = 512*3 #time
    with tf.io.TFRecordWriter(tfrecordfilename) as writer:
        for i in range(start, end):
            print(i)
            raw_data, label = open_excel(str(i))
            raw_data_len = len(raw_data)
            #print(raw_data_len)
            batch = int(raw_data_len / image_size)
            #print(batch)

            index = 0
            for i in range(batch):
                image_data = raw_data.loc[index:index+image_size - 1].values
                image_data = np.float32(image_data.flatten())
                index = index + image_size - 1
                features = {
                    'data': _float_feature(image_data),
                    'label': _int64_feature(label)
                }
                example = tf.train.Example(
                    features=tf.train.Features(feature=features))
                writer.write(example.SerializeToString())
        writer.close()





# Mapping function for tfrecord file
def map_func(example):
    # Feature attribute parsing table
    feature_map = {
                   'data': tf.io.FixedLenFeature([512*6], tf.float32),
                   'label': tf.io.FixedLenFeature([1], tf.int64)
                   }
    parsed_example = tf.io.parse_single_example(example, features=feature_map)
    data = parsed_example['data']
    # data = tf.io.decode_raw(parsed_example['data'], out_type=tf.float32)
    label = parsed_example['label']
    return data, label



if __name__ == '__main__':

    
    tfrecord_data_set = "total_men.tfrecords"
    
    gennerate_terecord_file(tfrecord_data_set, 0,60)
    print("end")
    
    
    dataset = tf.data.TFRecordDataset(tfrecord_data_set)
    dataset = dataset.map(map_func=map_func)
    print(dataset)

    