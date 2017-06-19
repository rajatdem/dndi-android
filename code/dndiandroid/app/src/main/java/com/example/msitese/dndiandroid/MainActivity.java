package com.example.msitese.dndiandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;
import com.example.msitese.dndigatheringzirks.LocationDataService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APP";
    private DNDIFramework dndi;
    private Bezirk bezirk;
    TextView tvConsole;
    private final EventSet eventSet = new EventSet(
            RawDataEvent.class
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tvConsole = (TextView) findViewById(R.id.tvConsole);

        BezirkMiddleware.initialize(this);
        bezirk = BezirkMiddleware.registerZirk("Main Activity");
        // initialize the DNDI framework
        dndi = new DNDIFramework(this);

        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof RawDataEvent){
                    final RawDataEvent rawDataEvent = (RawDataEvent) event;
                    long time= System.currentTimeMillis();
                    tvConsole.setText(rawDataEvent.toString());
                    Log.i(TAG, this.getClass().getName() + ":: \nReceived at " + time + rawDataEvent.toString());
                }
            }
        });
        bezirk.subscribe(eventSet);

    }

    public void onClickPull(View view){
        dndi.pullDataInBatch();
    }

    public void onClickPeriodic(View view){
        dndi.configPeriodicMode();
    }

    public void onClickEvent(View view){
        dndi.configEventMode();
    }

    public void onClickTwitterOAuth(View view) {
//        new GetTwitterTokenTask(this, dndi).execute();
    }

    /*
     * Build 1: Start with Request.
     * Build 2: Start Periodic updates.
     */

    public void onClickGPS(View view){
        //TODO: GET the GPS Coordinates.
        startService(new Intent(getBaseContext(), LocationDataService.class));
    }

    // Method to stop the service
    public void stopServiceGPS(View view) {
        stopService(new Intent(getBaseContext(), LocationDataService.class));
    }

}
