package com.example.msitese.dndigatheringzirks;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;

public class LocationDataService extends Service {

    private static final String TAG = "LocationDataGatheringZirk";
    private final IBinder mBinder = new LocationDataServiceBinder();
    private Bezirk bezirk;

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize the Bezirk Middleware and register as a Location data gathering Zirk
        BezirkMiddleware.initialize(this);
        bezirk = BezirkMiddleware.registerZirk(TAG);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    public LocationDataService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendBezirkEvent(Event evt){
        bezirk.sendEvent(evt);
    }

    public class LocationDataServiceBinder extends Binder{
        LocationDataService getService() {
            return LocationDataService.this;
        }
    }
}
