package edu.cmu.msitese.dndiandroid.datagathering.gps;

/*
 * Current Implementation: When the User presses the Button on the MainActivity -->
 * startService --> Starts the LocationDataService
 */

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import edu.cmu.msitese.dndiandroid.Utils;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;
import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

//TODO: 1] Ask user to give permission requestPermissions, onRequestPermissionsResult: IMPLEMENTATION IN LocationActivity
//TODO: 2] Pass the Context of the MainActivity to the this service and check shouldShowRequestPermissionRationale()
//TODO: 3] Collect and print the Location data
//TODO: 4] Periodic Collection of location data
//TODO: 5] Collecting the event data by setting up a minimum distance
//TODO: 6] Stop Listening to the Google Play Services when service is Killed.
//TODO: 7] Check if Location is enabled


public class LocationDataService extends Service implements ZirkEndPoint {

    private static final String TAG = "LocationDataGathrngZirk";
    private final IBinder mBinder = new LocationDataServiceBinder();

    /*
     * Choose the constant values for use
     * INTERVAL and FAST_INTERVAL in ms
     * DISPLACEMENT in m
     */
    private static long INTERVAL = 15000; //30secs default
    private static long FAST_INTERVAL = 15000; //30secs default
    private static long DISPLACEMENT = 5; //1000metres default
    private static String mode = "PERIODIC";

    private Bezirk bezirk;
    private RawData rawData;
    private RawDataEvent event;
    private String latitude;
    private String longitude;
    private Boolean status;
    private static Boolean update = true;
    private static CommandEvent cmdEvent;

    //Location Related fields. From Play Services Location Framework
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequestEvent = new LocationRequest();
    ;
    private LocationRequest mLocationRequestPeriodic = new LocationRequest();
    private LocationCallback mLocationCallback;

    public LocationDataService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

//        BezirkMiddleware.initialize(this);
        bezirk = BezirkMiddleware.registerZirk("LocationGathering");
        EventSet eventSet = new EventSet(CommandEvent.class);
        bezirkListener(eventSet);
        createLocationRequestEvent();
        createLocationRequestPeriodic();
        createLocationCallBack();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void createLocationCallBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();
                longitude = String.valueOf(mLastLocation.getLongitude());
                latitude = String.valueOf(mLastLocation.getLatitude());
                sendMessage();
            }
        };
    }

    private void createLocationRequestEvent() {
//        mLocationRequestEvent = new LocationRequest();
        mLocationRequestEvent.setInterval(INTERVAL);
        mLocationRequestEvent.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequestEvent.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationRequestPeriodic() {
//        mLocationRequestPeriodic = new LocationRequest();
        mLocationRequestPeriodic.setInterval(INTERVAL);
        mLocationRequestPeriodic.setFastestInterval(FAST_INTERVAL);
        Log.i(TAG, mLocationRequestPeriodic.getSmallestDisplacement()+"");
        mLocationRequestPeriodic.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkLocationPermission(intent);
        return START_NOT_STICKY;
    }

    //public int onStartCommand(Intent intent, int flags, int startId)
    public void checkLocationPermission(Intent intent) {
        // Let it continue running until it is stopped.
//        Toast.makeText(this, "GPS Service Started", Toast.LENGTH_LONG).show();
        //Check Android VERSION < 23 and start the mode set up as the default one.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            switch (mode) {
                case "PULL":
                    getLocation();
                    break;
                case "EVENT":
                    getLocationUpdatesEvent();
                    break;
                case "PERIODIC":
                    getLocationUpdatesPeriodic();
                    break;
                case "NONE":
                    break; //No need to gather GPS data in this mode.
                default:
                    getLocation();
            }
        }

        //Check Android VERSION > 23 and start the mode set up as the default one.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Intent From the MainActivity
            if ((status = intent.getBooleanExtra("Status", Boolean.parseBoolean(null))) != null) {

                if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) == PERMISSION_DENIED &&
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_DENIED) {

                    Intent dialogIntent = new Intent(this, LocationActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);

                } else {
                    switch (mode) {
                        case "PULL":
                            getLocation();
                            break;
                        case "EVENT":
                            getLocationUpdatesEvent();
                            break;
                        case "PERIODIC":
                            getLocationUpdatesPeriodic();
                            break;
                        case "NONE":
                            break; //No need to gather GPS data in this mode.
                        default:
                            getLocation();
                    }
                }

            } else { //Intent From the LocationActivity

                if ((ContextCompat.checkSelfPermission(this, //if no permission was allowed. Send empty Bezirk event
                        Manifest.permission.ACCESS_FINE_LOCATION)) == PERMISSION_DENIED &&
                        ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_DENIED) {

                    if (!update) {
                        sendEmptyMessage();
                    }

                } else {
                    switch (mode) {
                        case "PULL":
                            getLocation();
                            break;
                        case "EVENT":
                            getLocationUpdatesEvent();
                            break;
                        case "PERIODIC":
                            getLocationUpdatesPeriodic();
                            break;
                        case "NONE":
                            break;
                        default:
                            getLocation();
                    }
                }
            }
        }
    }

    @Override
    public boolean stopService(Intent name) {

        //TODO: Stop Listening to Google Play Services
        Log.i(TAG, "Stopping GPS services!!!");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
        return super.stopService(name);
    }

    // Warnings suppressed as this path will be taken only after permission check done in the
    // onStartCommand
    private void getLocationUpdatesEvent() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequestEvent,
                    mLocationCallback, null);
        }
    }

    private void getLocationUpdatesPeriodic() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequestPeriodic,
                    mLocationCallback, null);
        } else {
            Log.i(TAG, "Permission no longer available");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
        Log.i(TAG, "Stopping GPS service!");
        Toast.makeText(this, "GPS Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendMessage(){
        //getLastLocation();
        rawData = new RawData();
        rawData.setLocation("{\"latitude\":\""+latitude+"\",\"longitude\":\""+longitude+"\"}");
//        Log.i(TAG, "Sending over the Middleware");
//        Log.i(TAG, latitude);
//        Log.i(TAG, longitude);
        event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        event.hasLocation = true;
        event.appendRawData(rawData);
        bezirk.sendEvent(event);
    }

    public void sendEmptyMessage(){
        rawData = new RawData();
        event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        bezirk.sendEvent(event);
    }

    public class LocationDataServiceBinder extends Binder{
        LocationDataService getService() {
            return LocationDataService.this;
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        mLastLocation = task.getResult();
                        if(mLastLocation != null) {
                            latitude = String.valueOf(mLastLocation.getLatitude());
                            longitude = String.valueOf(mLastLocation.getLongitude());
                            sendMessage();
                        }
                        else{
                            //In case Last Location is not available with the Google Play Services.
                        }
                    }
                });
    }

    public void bezirkListener(EventSet eventSet){

        eventSet = new EventSet(CommandEvent.class);
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof CommandEvent){

                    final CommandEvent commandEvent = (CommandEvent) event;
                    CommandEvent.CmdType cmdType = commandEvent.type;

                    if (!commandEvent.target.equals("GPS")) {
                        return;
                    }

                    Log.i(TAG, this.getClass().getName() + ":: received:" + cmdType);
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) == PERMISSION_DENIED &&
                            ContextCompat.checkSelfPermission(getApplicationContext(),
                                    Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_DENIED) {

                        Log.i(TAG, "Location Permission Denied");

                    } else {
                        switch (cmdType) {
                            case CMD_PERIODIC:
                                mode = Utils.MODE.PERIODIC.toString();
                                try {
                                    FAST_INTERVAL = Integer.parseInt(commandEvent.extra);
                                } catch (NumberFormatException nExp) {
                                    //Continue to Use default Value
                                }
                                createLocationRequestPeriodic();
                                getLocationUpdatesPeriodic();
                                Log.i(TAG, "Mode:" + mode + " | Period:" + FAST_INTERVAL);
                                break;
                            case CMD_PULL:
                                mode = Utils.MODE.BATCH.toString();
                                getLocation();
                                break;
                            case CMD_EVENT:
                                mode = Utils.MODE.EVENT.toString();
                                try {
                                    DISPLACEMENT = Integer.parseInt(commandEvent.extra);
                                } catch (NumberFormatException nExp) {
                                    //Continue to Use default Value
                                }
                                createLocationRequestEvent();
                                getLocationUpdatesEvent();
                                Log.i(TAG, "Mode:" + mode + " | DeltaDistance:" + DISPLACEMENT);
                                break;
                        }
                    }
                }
            }
        });
        bezirk.subscribe(eventSet);
    }
}
