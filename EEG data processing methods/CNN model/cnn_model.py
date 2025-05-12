from tensorflow import keras
from tensorflow.keras import layers



# CNN mode
def al_model():
    input_data = keras.Input(shape=(512*3, 6, 1))
    # Convolutional Layer
    x = layers.Conv2D(32, (3, 3), padding='same', activation='relu')(input_data)  
    x = layers.MaxPooling2D()(pool_size=(2, 2), strides=(2, 2))  
    x = layers.Conv2D(64, (3, 3), strides=(2, 2), padding='same', activation='relu')(x)  # Output: (500, 3, 64)
    x = layers.MaxPooling2D(pool_size=(2, 2), strides=(2, 2))(x)  
    x = layers.Conv2D(128, (3, 3), strides=(2, 2), padding='same', activation='relu')(x)  
    x = layers.MaxPooling2D(pool_size=(2, 2), strides=(2, 2))(x)  # Output: (125, 1, 128)
    # Fully Connected Layer
    x = layers.Flatten()(x)  # Output: (125*1*128)= 16000
    x = layers.Dense(256, activation='relu')(x)  # 1@1x256
    x = layers.Dropout(0.5)(x)  
    x = layers.Dense(52, activation='relu')(x)    # 1@1x52
    model_output = layers.Dense(3, activation='softmax')(x)

    model = keras.Model(input_data, model_output)
    return model

