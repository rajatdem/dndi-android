package com.example.msitese.dndiandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the Bezirk service
        BezirkMiddleware.initialize(getApplicationContext());

        // register with Bezirk middleware to get an instance of Bezirk API.
        bezirk = BezirkMiddleware.registerZirk("Main Zirk");

        final EventSet eventSet = new EventSet(RawDataEvent.class);
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                final RawDataEvent rawDataEvent = (RawDataEvent) event;
                String content = rawDataEvent.getContent();
                long time= System.currentTimeMillis();
                Log.i(TAG, this.getClass().getName() + ":: Content: " + content + " at " + time);
            }
        });
        bezirk.subscribe(eventSet);

//        startService(new Intent(getBaseContext(), TwitterService.class));
        Log.i(TAG, this.getClass().getName() + ":: Wait...");
    }

    public void onClickPull(View view){
        final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PULL);
        bezirk.sendEvent(evt);
    }

    public void onClickPeriodic(View view){
        final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC);
        bezirk.sendEvent(evt);
    }

    public void onClickEvent(View view){
        final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT);
        bezirk.sendEvent(evt);
    }
}
