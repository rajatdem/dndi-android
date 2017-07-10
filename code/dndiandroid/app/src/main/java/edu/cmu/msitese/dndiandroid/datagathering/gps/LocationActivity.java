package edu.cmu.msitese.dndiandroid.datagathering.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LocationActivity extends Activity {

    private final static String TAG = "LocationActivity";
    private static final int REQUEST_CODE_PERMISSION = 2;
    private static Boolean mPermissionStatus = false;
    private String mPermissionFine = Manifest.permission.ACCESS_FINE_LOCATION;
    private String mPermissionCoarse = Manifest.permission.ACCESS_COARSE_LOCATION;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Started Loaction Activity");

        try {
            if ( (ActivityCompat.checkSelfPermission(this, mPermissionFine)
                    != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, mPermissionCoarse)
                    != PackageManager.PERMISSION_GRANTED) ) {

                ActivityCompat.requestPermissions(this,
                        new String[]{mPermissionFine}, REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            }
            else{
                Log.i(TAG, "Permission Granted and exiting");
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                // Success Stuff here
                Log.i(TAG, "Permission Granted");
                mPermissionStatus = true;
                finish();
            }
            else{
                // Failure Stuff
                Log.i(TAG, "Permission Denied");
                mPermissionStatus = false;
                finish();
            }
        } else {
            Log.i(TAG, "Permission Denied");
            mPermissionStatus = false;
            finish();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void finish() {
        super.finish();
        Log.i(TAG, "Finish Called");
        if(shouldShowRequestPermissionRationale(mPermissionFine)){
            //Fill an intent with the permission
            Intent serviceIntent = new Intent(getBaseContext(), LocationDataService.class);
            serviceIntent.putExtra("Status", mPermissionStatus);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
