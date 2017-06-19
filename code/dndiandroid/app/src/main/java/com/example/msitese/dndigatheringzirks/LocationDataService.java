package com.example.msitese.dndigatheringzirks;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.example.msitese.dndiandroid.ConfigService;
import com.example.msitese.dndiandroid.RawData;
import com.example.msitese.dndiandroid.RawDataEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class LocationDataService extends Service {

    private static final String TAG = "LocationDataGatheringZirk";
    private final IBinder mBinder = new LocationDataServiceBinder();
    private RawData rawData;
    private FusedLocationProviderClient mFusedLocationClient;


    public LocationDataService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "GPS Service Started", Toast.LENGTH_LONG).show();
        sendMessage();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "GPS Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendMessage(){
        rawData = new RawData();
        rawData.setLocation("Pittsburgh");
        RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        event.appendRawData(rawData);
        this.sendBezirkEvent(event);
    }

    public class LocationDataServiceBinder extends Binder{
        LocationDataService getService() {
            return LocationDataService.this;
        }
    }
}
