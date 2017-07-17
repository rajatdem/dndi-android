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

import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.datainference.keyword.KeywordMatchService;
import edu.cmu.msitese.dndiandroid.datanormalization.location.GeocodingService;
import edu.cmu.msitese.dndiandroid.event.ResultEvent;


/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class ZirkManagerService extends Service {

    private static final String TAG = "ZIRK";

    // Depending on the device and the current load on the device, Bezirk middleware could take
    // anywhere between 300-1500 ms to initialize completely. This delay (in ms) is used to ensure
    // that the initialization has completed before zirks are registered with the middleware.
    private static final int BEZIRK_INITIALIZATION_DELAY = 1500;
    public static final String ACTION = "edu.cmu.msitese.dndiandroid.ConfigService";

    private Bezirk bezirk;
    private final EventSet eventSet = new EventSet(
            ResultEvent.class
    );

    private final IBinder mBinder = new ConfigServiceBinder();
    private final Class<?>[] services = {
            TwitterService.class,
            LocationDataService.class,
            GeocodingService.class,
            KeywordMatchService.class
    };

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {

        Log.wtf(TAG, "STARTING OTHER SERVICES");
        // initialize the Bezirk service
        BezirkMiddleware.initialize(getBaseContext());


        // register with Bezirk middleware to get an instance of Bezirk API.
        bezirk = BezirkMiddleware.registerZirk("ZirkManager");

        // config event receive callbacks
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
        // initialize the zirk after the middleware has initialized
        new Timer().schedule(new ZirkInitializer(), BEZIRK_INITIALIZATION_DELAY);
    }


    private class ZirkInitializer extends TimerTask {
        @Override
        public void run() {
            // register with Bezirk middleware to get an instance of Bezirk API.
            bezirk = BezirkMiddleware.registerZirk("ConfigZirk");

            eventSet.setEventReceiver(new EventSet.EventReceiver() {

                @Override
                public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                    if (event instanceof ResultEvent) {
                        final ResultEvent resultEvent = (ResultEvent) event;
                        Intent intent = new Intent(ACTION);
                        intent.putExtra("result", DNDIFramework.KEYWORD_MATCH);
                        intent.putStringArrayListExtra("keywords", resultEvent.getMatchList());
                        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast
                                (intent);
                    }
                }
            });
            bezirk.subscribe(eventSet);

            for (Class<?> cls : services) {
                startService(new Intent(getBaseContext(), cls));
            }
            Log.i(TAG, this.getClass().getName() + ":: wait...");
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

    public void sendBezirkEvent(Event evt) {
        bezirk.sendEvent(evt);
    }

    public class ZirkManagerServiceBinder extends Binder {
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
       public ZirkManagerService getService() {
            return ZirkManagerService.this;
        }
    }

    public void stopGPSService() {
        Log.i(TAG, "Stopping GPS services!!!");
        stopService(new Intent(getBaseContext(), LocationDataService.class));
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setBezirkInstance(Bezirk bezirk) {
        this.bezirk = bezirk;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setEventSetSubscriptionHandler(EventSet.EventReceiver receiver) {
        eventSet.setEventReceiver(receiver);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public EventSet.EventReceiver getEventSetSubscriptionHandler() {
        return eventSet.getEventReceiver();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void stopChildrenService() {
        for (Class<?> cls : services) {
            stopService(new Intent(getBaseContext(), cls));
        }
    }
}
