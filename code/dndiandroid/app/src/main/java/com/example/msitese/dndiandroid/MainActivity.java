package com.example.msitese.dndiandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


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
}
