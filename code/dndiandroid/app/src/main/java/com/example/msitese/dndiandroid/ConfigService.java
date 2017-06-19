package com.example.msitese.dndiandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;


/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class ConfigService extends Service {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;
    private final EventSet eventSet = new EventSet(
            RawDataEvent.class
    );

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
        bezirk = BezirkMiddleware.registerZirk("ConfigZirk");

        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof RawDataEvent){
                    final RawDataEvent rawDataEvent = (RawDataEvent) event;
                    long time= System.currentTimeMillis();
                    Log.i(TAG, this.getClass().getName() + ":: \nReceived at " + time + rawDataEvent.toString());
                }
            }
        });
        bezirk.subscribe(eventSet);

        for(Class<?> cls: services){
            startService(new Intent(getBaseContext(), cls));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setBezirkInstance(Bezirk bezirk){
        this.bezirk = bezirk;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setEventSetSubscriptionHandler(EventSet.EventReceiver receiver){
        eventSet.setEventReceiver(receiver);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public EventSet.EventReceiver getEventSetSubscriptionHandler(){
        return eventSet.getEventReceiver();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void stopChildrenService(){
        for(Class<?> cls: services){
            stopService(new Intent(getBaseContext(), cls));
        }
    }
}
