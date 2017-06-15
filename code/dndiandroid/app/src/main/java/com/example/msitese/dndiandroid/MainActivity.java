package com.example.msitese.dndiandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.msitese.dndigatheringzirks.LocationDataService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "APP";
    private DNDIFramework dndi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // initialize the DNDI framework
        dndi = new DNDIFramework(this);
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
