package edu.cmu.msitese.dndiandroid.frameworkinterface;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.event.ResultEvent;


/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class ZirkManagerService extends Service {

    private static final String TAG = "ZIRK";
    public static final String ACTION = "edu.cmu.msitese.dndiandroid.ZirkManagerService";

    private Bezirk bezirk;
    private final EventSet eventSet = new EventSet(
            ResultEvent.class
    );

    private final IBinder mBinder = new ConfigServiceBinder();
    private final Class<?> [] services = {
            TwitterService.class,
    };

    /** Called when the service is being created. */
    @Override
    public void onCreate() {

        // initialize the Bezirk service
        BezirkMiddleware.initialize(getBaseContext());

        // register with Bezirk middleware to get an instance of Bezirk API.
        bezirk = BezirkMiddleware.registerZirk("ConfigZirk");

        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof ResultEvent){
                    final ResultEvent resultEvent = (ResultEvent) event;
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("result", DNDIFramework.KEYWORD_MATCH);
                    intent.putStringArrayListExtra("keywords", resultEvent.getMatchList());
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                }
            }
        });
        bezirk.subscribe(eventSet);

        for(Class<?> cls: services){
            startService(new Intent(getBaseContext(), cls));
        }
        Log.i(TAG, this.getClass().getName() + ":: wait...");
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

    public void sendBezirkEvent(Event evt){
        bezirk.sendEvent(evt);
    }

    public class ConfigServiceBinder extends Binder {
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
       public ZirkManagerService getService() {
            return ZirkManagerService.this;
        }
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
