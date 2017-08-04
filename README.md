# Data normalization and data inference framework

A data preparation framework built on top of Bezirk middleware 

## Overview

add the project overview here, include a vrief explanation of zirk here

## Dependencies

include all third-party libraries required to build the project

## Installation

specify how to include the framework and run along with the android project

## Example

### Build a new data preparation Zirk

specify how to plug in a new data preparation unit made by developer into the framework

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

specify how to update keywords in the framework

### Test the new components

specify how to unit test and integration test the framework

### Maintain application lifecycle for resource cleanup

specify the rules an application should follow when use this framework

## For more information

put any reference here
