# Data Preparation framework

A data preparation framework built on top of Bezirk Middleware by the MSIT-ESE team at Carnegie Mellon University's Software Engineering Department.
The team consists of 3 members

[Yu-Lun](https://github.com/stormysun513/)

[Sai Chandana](https://github.com/SaiHariChandana)

[Rajat Mathur](https://github.com/rajatdem)

## Overview

DNDI is an Android framework that makes managing Zirks (an Android service using the Bezirk middleware for communication among other services) easier. One can create their own Zirk and plug into the framework easily. As for the developer, it provides a simple interface to collect multiple data sources with a single interface.

## Example Use Cases

- Learn the user interest from historical tweets and fuse with location data
- Update of fused results when there is a location change
- Real-time update of interest categories with new tweets from the user


## Architecture

### Context Diagram
The Data preparation infrastructure has the following context diagram.

![Context Diagram](https://github.com/stormysun513/dndi-android/blob/develop/documents/images/contextDiagram%20-%20Page%201.png)

In this diagram we indentify the following:

**Users**
1. Mobile user: End user of the device 
2. Application developers: Developers of Android application

**Android Device**
This is the system on which the framework is developed

**Third party applications**
These are applications that use the data preparation infrastructure results to make user recommendations. Ex Yelp 

**Sensor sources**
These can be both hardware or software sensors. 
1. *Hardware sensors*: Sensors available on the smartphone such as GPS, accelerometer etc...
2. *Software sensors*: Sensors such as web services, social media platforms like Facebook, Twitter, LinkedIn etc...

**Data Preparation infrastrucutre**
The framework that 
1. Gathers users data from different sensor sources
2. Provides some meaning to the data by normalizing it 
3. Infers users interests and preferences

**Bezirk Middleware**
The Bezirk platform is a publish-subscribe based middlware used for secure flow of data and keep users information in control of user. (https://business.bezirk.com/platform.html) 

### Proposed Solution
The diagram below represents a run time view of the proposed architecture. 

![Run time view of the system](https://github.com/stormysun513/dndi-android/blob/develop/documents/images/Runtime%20View_v1.3.png)

The Framework is a library that is used by third party applications. It uses the Bezirk middleware and follows publish-subscribe model to interact between different Zirks. 
 
A Zirk is a process that wraps around functional units and provides abstraction for the technologies used by the Zirks. A Zirk also helps restrict the input and output formats for communication. 

In the above view, there are following Zirks:
1. *Data Gathering*: Facebook Zirk, Twitter Zirk, GPS Zirk.
2. *Data Normalization*:Normalization Zirk that can do text and location based normalization.
3. *Data Inference*: Inference Zirk provides inferences based on the data provided by normalization.

The information gathered by each of these data gathering Zirks are sent to the normalization Zirks in a unified format elaborated below. The information is gathered in three ways: 
1. *Batch Mode*: Gather historical data of the user. Example: Last 6 months of tweets.
2. *Event Mode*: Gather data whenever there is an event. Example: Every time user tweets something.
3. *Periodic Mode* Gather data after elapsed period of time. Example: Gather all users new tweets at the end of the day.

The information in the normalization Zirks are then normalized based on: 
1. *Text*: Parse the textual information to indentify key words. The pre-defined set of keywords are stored on the phone.
2. *Location*: Parse the location coordinates and provide place name, area name for those coordinates.

The inference Zirk will look at the normalized results, checks for the categories of the keywords idnetified and infers the users interests. 

## Dependencies

- twitter4j (4.0.6)
- android-zirk-proxy (3.2.0-snapshot)
- google-play-services (11.0.1)
- ranga543/yelp-fusion-client (0.1.2)

## Installation

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

## Packaging

The Android library is packaged as a **.aar** file, which is an acronym for Android Archive. This AAR can be used by other Android projects by simply importing aspart of the build.

### Packaging the Framework into a Library: Process used to package the framework developed

- Open the build.gradle file for the Module:app under the gradle folder Android Studio's app view.
- Change the ```apply:plugin: 'com.android.application'``` to ```apply plugin: 'com.android.library'``` to compile the framework as a library.
- Sync the project by clicking on the ```Sync Now``` button appearing on the top of the gradle file. currently.
- To build and generate the AAR: Click on **Project --> Build --> Build APK** and select the location to store the AAR file.

### Using the Library in your own Android project.

- Click File > New > New Module.
- Click Import .JAR/.AAR Package then click Next.
- Enter the location of the compiled AAR or JAR file then click Finish.
- In the settings.gradle, include : ```include ':app', ':my-library-module'``` at the top of the file.
- In the build.gradle file, add 
```
dependencies {
    compile project(":my-library-module")
}
```
- Sync the Project

_Reference_: [Packaging Android Framework](https://developer.android.com/studio/projects/android-library.html)

## Example

### Build a new data preparation zirk

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
```JAVA
@Override
public void onCreate() {
    super.onCreate();
    bezirk = BezirkMiddleware.registerZirk("ZIRK_NAME");
    bezirkListener();
}
```
- Implement the ```bezirkListener() method```
```JAVA
public void bezirkListener() {
    eventSet.setEventReceiver(new EventSet.EventReceiver() {
        @Override
        public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
            final CommandEvent commandEvent = (CommandEvent) event;
            CommandEvent.CmdType cmdType = commandEvent.type;
            Log.i(TAG, this.getClass().getName() + "::received" + cmdType);
            //TODO: Implement your code to handle different modes of operation
            
        }
    }
}
```
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
- Open ```ZirkManagerService.java``` in ```../frameworkinterface package```. Add the your Zirk Service class name in the ```private final Class<?>[] services``` field'd declarartion. This field stores the name of the Service classes that need to be started when the framework is started.

### Configure access token for data gathering zirk (if required)

specify how to add an interface to pass access token from application to the target gathering zirk

1. Use gradle.properties to store the needed API_KEY and API_SECRET values
2. Within the build.gradle file use BuildConfig class to create the corresponding fields.
```Java
buildConfigField(“String”,”EXAMPLE_API_KEY”,EXAMPLE_API_KEY)
```
3. Use the BuildConfig fields to load the key values 
```Java
Private static final String EXAMPLE_API_KEY = BuildConfig.EXAMPLE_API_KEY;
```
4. Use Android shared preferences to store and retrieve keys as needed.

### Configure different modes on data gathering zirk

specify how to configure a gathering zirk into a particular mode

1. Use the mode to send corresponding event.
2. Create an function to prepare the required event on recieving the mode for the zirk. Add the function inside the DNDIFramework class.

Here is an example to send the pull event to confiugre pull mode 

```Java
public void configRequiredMode(){
	if(isBound){	
		//Prepare the pull event to send over middleware
		final CommandEventevt = new CommandEvent(CommandEvent.CmdType.CMD_PULL);
		
		//Use the ZirkManagerService to send Bezirk event 
		mZirkManagerService.sendBezirkEvent(evt);	
}
```
2. The interface from which the mode is being configured will call an instance of dndi to access the above created function. 

```Java
someFunctionToSelectPullMode { dndi.pullDataInBatchAll(); }  
```

3. The Bezirk middleware will publish the event 

4. To receive the event and check the mode configured
```Java
eventSet.setEventReciever((event, zirkEndpoint)->{
 final CommandEvent commandEvent = (CommandEvent) event;
 CommandEvent.CmdType cmdType = commandEvent.type;		
  
  switch (cmdType)
  {
    case CMD_PULL:
       break;
    case CMD_PERIODIC:
       break;
    case CMD_EVENT:
	     break;
  }
  mBezirk.subscribe(eventSet);
}
	
 ```

### Data model for communication between data gathering and data normalization

[Sai Chandana](https://github.com/SaiHariChandana)

specify the rules a data gathering zirk should follow to get normalization zirk work for you

### Implement application callbacks when there is a notification

Any application related events (e.g. KEYWORD\_MATCH, LOCATION\_UPDATE) will be collect by the ZirkManagerService first and forward to the DNDIFramework through broadcast intents. The current version has implemented interfaces for these two events. One just implements the DNDIFrameworkListener interfaces for retrieving these information. If one would like to add more events, one should first declare a new method in the DNDIFrameworkListener and add corresponding code in the broadcast receiver in DNDIFramework. 

```java
// the broadcast receiver callback that parse the intent and call corresponding callback functions
private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        String result = intent.getStringExtra(RESULT);
        switch (result){
            case KEYWORD_MATCHED:
            ArrayList<String> keywords = intent.getStringArrayListExtra(KEYWORD_MATCHED);
            if(mContext instanceof DNDIFrameworkListener){
                ((DNDIFrameworkListener) mContext).onKeywordMatch(keywords);
            }
            break;
        case RAW_LOCATION:
            Location location = intent.getParcelableExtra(RAW_LOCATION);
            if(mContext instanceof DNDIFrameworkListener){
                ((DNDIFrameworkListener) mContext).onLastLocationUpdate(location);
            }
            break;
        case ERROR:
        default:
            break;
        }
    }
};
```

With regard to the sender side, one can define one's own intent keys and send through th broadcast intent. The example code below is in the bezirk receiver in ZirkManagerService.

```java
final KeywordMatchEvent keywordMatchEvent = (KeywordMatchEvent) event;
Intent intent = new Intent(ACTION);
intent.putExtra(DNDIFramework.RESULT, DNDIFramework.KEYWORD_MATCHED);
intent.putStringArrayListExtra(DNDIFramework.KEYWORD_MATCHED, keywordMatchEvent.getMatchList());
LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
```

### Define your own event

To create your own event
1. Go to event folder in main/java/edu/cmu/msitese/dndiandroid/event/
2. Create a java file for your event using the format  event_name.java
   Sample code for event_name.java 
   
```Java
Public class event_name extends Event {
       // your code
       ......
}

```
3. Add the event of interest within an eventset of type eventSet

```Java
Private final EventSet eventset = new EventSet {
	event_name.class
}

```

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

One may consider testing the new Zirk to the framework. One can refer to some androidtest files in the test directory. We illustrate the way how a Zirk is tested. 

Each testcase has its own context, so one has to initialize the Bezirk Middleware everytime. Later, create an object used to notify the waiting main thread. The reason why we implement th testcase in this way is that Bezirk requires time for initialization. A TimerTask is required to have a slight delay before starting testing. After having this code structure, one can put the testing logics in the `run()` function overrided by the TimerTask.

```java
@Test(timeout = 30000)
public void testKeywordMatchEvent() throws InterruptedException {
    // initialize the Bezirk service for testing
    BezirkMiddleware.initialize(getContext());
	
    // sync object used to check whether timertask completes before the timeout budget
    final Object syncObject = new Object();
	
    new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
	
            // Bind the service and grab a reference to the binder.
            IBinder binder = bindService(new Intent(getContext(), KeywordMatchService.class));
            assertNotNull(binder);
		
            // Get service instances
            KeywordMatchService service = ((KeywordMatchService.KeywordMatchServiceBinder) binder).getService();
            assertNotNull(service);

            // TODO: add testing logic here
		
            BezirkMiddleware.stop();
			
            synchronized (syncObject) {
                syncObject.notify();
            }
        }
    }, 1000);
	
    // wait for timertask to complete
    synchronized (syncObject) {
        syncObject.wait();
    }
}
```

Although most Zirks are running independently and no other activities or services will bind to them, one may still implement the `onBind` method and return a IBinder for testing purpose. The latest Android SDK has provided an anotation for methods that is defined for testing only. One can check the usage of anotation `@VisibleForTesting(otherwise = VisibleForTesting.NONE)`.

### Maintain the lifecycle for resource cleanup and power saving

The communication between ZirkManagerService and a app forground activity is done through Android broadcast intent. The ZirkManagerService gathers information needed for the activity to refresh the GUI components and send it. The DNDIFramework is owned by the main activity so it is in the same memory space as the activity. It registers a broadcast receiver with a particular intent filter to collect intents sent by the ZirkManagerService only.

The application needs the information only when it is running in the foreground, because there is no need to update GUI when running in the background (android service). The following code snippet applies to foreground applicaiton only. If the DNDIFramework instance is owned by a service. It may keep listening to the broadcat intent throughtout the entire lifecycle.

When an applicaiton is removed from the foreground, the `onPause` function is called. When an application is back to the foreground, the `onResume` function is called. Therefore, one may consider override them and call the following methods.

```java
@Override
protected void onResume(){
    super.onResume();
    dndi.resume();
}

@Override
protected void onPause(){
    dndi.pause();
    super.onPause();
}

@Override
protected void onDestroy(){
    dndi.stop();
    super.onDestroy();
}
```

Sometimes, one may have multiple activities in an single application. Each activity may need some information from the DNDIFramework. One can simply instantiate another DNDIFramework and it will bind to the ZirkManagerService, which is design in a singleton pattern.

## For more information

put any reference here
