package com.example.msitese.dndiandroid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import java.util.List;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class ConfigService extends Service {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;
    private final IBinder mBinder = new ConfigServiceBinder();
    private final Class<?> [] services = {
//            TwitterService.class,
    };

    /** Called when the service is being created. */
    @Override
    public void onCreate() {

        // initialize the Bezirk service
        BezirkMiddleware.initialize(this);

        // register with Bezirk middleware to get an instance of Bezirk API.
        bezirk = BezirkMiddleware.registerZirk("Configuration Zirk");


        final EventSet eventSet = new EventSet(RawDataEvent.class);
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                final RawDataEvent rawDataEvent = (RawDataEvent) event;
                long time= System.currentTimeMillis();
                Log.i(TAG, this.getClass().getName() + ":: Received message at " + time);
                Log.i(TAG, this.getClass().getName() + ":: Content: " + rawDataEvent.content);
            }
        });
        bezirk.subscribe(eventSet);

        for(Class<?> cls: services){
            startService(new Intent(getBaseContext(), cls));
        }
        Log.i(TAG, this.getClass().getName() + ":: Wait...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ConfigServiceBinder extends Binder {
        ConfigService getService() {
            return ConfigService.this;
        }
    }

    public void sendBezirkEvent(Event evt){
        bezirk.sendEvent(evt);
    }
}
