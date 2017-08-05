# Data normalization and data inference framework

A data preparation framework built on top of Bezirk middleware 

## Overview

add the project overview here, include a vrief explanation of zirk here

## Dependencies

include all third-party libraries required to build the project

## Installation

specify how to include the framework and run along with the android project
- Download or clone the repository [Github Repository](https://github.com/stormysun513/dndi-android)
- Download and Install Android Studio [Android Studio](https://developer.android.com/studio/index.html)
- Import the project into Android Studio from the folder where the repository was cloned on the local device.
- Create and place the ```gradle.properties``` under the root project folder. Include the API keys for the different services here.
Sample properties file:
```PROPERTIES
org.gradle.jvmargs=-Xmx1536m


TWITTER_API_KEY = "FILL_YOUR_OWN"
TWITTER_API_SECRET = "FILL_YOUR_OWN"
TWITTER_ACCESS_TOKEN = "FILL_YOUR_OWN"
TWITTER_ACCESS_SECRET = "FILL_YOUR_OWN"
TWITTER_USER_ID = "FILL_YOUR_OWN"
FACEBOOK_API_KEY = "FILL_YOUR_OWN"
GOOGLE_PLACE_API_KEY = "FILL_YOUR_OWN"
```
- This project uses SDK Version of 25 for Android. Build tools version: 25.0.2 
This can be updated from

**Tools --> Android --> SDK Manager --> SDK Tools (from the tabs in the Window) --> _Select_ Android SDK Built-Tools and _check the_ Show Package Details _box on the right bottom_ --> _Select_ 25.0.2 --> Apply --> OK**

## Example

### Build a new data preparation Zirk

- Install Android Studio [Android Studio Setup](https://developer.android.com/studio/intro/migrate.html)
- Import the project in Android Studio. 
- Create a new package under ```edu.cmu.msitese.dndiandroid.datagathering``` with the name of the sensor source.
- Create a new Android Service by
   **Right click on the new package created --> New --> Service --> Service**
- This creates a new Java class in the package. Name it as ```[SensorName]Service.java```.
- Implement ```ZirkEndPoint interface``` and ensure the class extends the ```Service class```
- Create a TAG for the class. This helps in logging and debugging later
This can be done by creating a field in the class.
```private static final String TAG = "LocationDataGathrngZirk";```
- Override the ```onCreate()``` method of the service and register the Zirk to the Bezirk Middleware.
Also call the method for listening to events from the ```ZirkManagerService class```, which sends the mode of operation to the respective Zirks.
![onCreate()](https://github.com/stormysun513/dndi-android/blob/rajatdem/documents/images/onCreate%20in%20Zirk.png)
- Implement the ```bezirkListener() method```
![bezirkListener](https://github.com/stormysun513/dndi-android/blob/rajatdem/documents/images/bezirkListener.png)
This method implements handling of the ```CommandEvent``` received by the sensor Zirk from ```ZirkManagerService```
Different flow of execution can be accomodated depending on the type of Mode received by the Zirk. 
- Implement a ```sendMessage() method``` to send Bezirk Events to Normalization Zirks from the Data Gathering Zirk
```Java
public void sendMessage () {
        rawData = new RawData();
        rawData.setLocation(""); //Optional: Can be null
        rawData.setDate(""); //Optional: Can be null
        rawData.setText(""); //Optional: Can be null
        //Create a RawDataEvent with the current mode of operation
        RawDataEvent rawDataEvent = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        //Pack RawData
        rawDataEvent.appendRawData(rawData);
        event.hasLocation = true;
        event.hasText = true;
        //Send RawDataEvent over Bezirk Middleware to the Normalization Zirk
        bezirk.sendEvent(rawDataEvent);
    }
```

### Configure access token for data gathering zirk (if required)

specify how to add an interface to pass access token from application to the target gathering zirk

### Configure different modes on data gathering zirk

specify how to configure a gathering zirk into a particular mode

### Data model for communication between data gathering and data normalization

specify the rules a data gathering zirk should follow to get normalization zirk work for you

### Data model for communication between data normalization and data inference

specify the rules a data normalization zirk should follow to get inferecne zirk work for you

### Implement application callbacks when there is a notification

specify how to implement one's own business logic when there is a keyword match event 

### Define your own event

specify the rules one has to abide by if one would like to create one's own zirk

### Update the keyword-category map

Keywords are stored in a JSON file as part of the Android application. These are used by the textual normalization
Zirk to compare them with the sensor data feed of the user. The keywords are categorized under a category.

_Sub categories not supported yet by the Text Normalization Zirk_

Create a file called ```keywords.json``` in your Android project.
This can be created under **main/res/raw/keywords.json** folder in the Android project.

You can create multiple entities falling under one broader category of entities

Format of the JSON file

```
[
  {
    "category": "@CATEGORY_NAME_1",
    "keywords": [
      "ENTITY_1",
      "ENTITY_2",
      . . . .
      "ENTITY_N"
    ]
  },
  . . . .
  {
    "category": "@CATEGORY_NAME_2",
    "keywords": [
      "ENTITY_1",
      "ENTITY_2",
      . . . .
      "ENTITY_N"
    ]
  }
]
```

### Test the new components

specify how to unit test and integration test the framework

### Maintain application lifecycle for resource cleanup

specify the rules an application should follow when use this framework

## For more information

put any reference here
