package edu.cmu.msitese.dndiandroid;

/**
 * Created by rajatmathur on 8/13/17.
 */

import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Timer;
import java.util.TimerTask;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static org.junit.Assert.*;

import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationActivity;
import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

//@RunWith(AndroidJUnit4.class)
public class LocationActivityTest {
    @Rule
    public ActivityTestRule<LocationActivity> mActivityRule
            = new ActivityTestRule<>(LocationActivity.class);

    public void revokeLocationPermission() {
        // In M+, trying to call a number will trigger a runtime dialog. Make sure
        // the permission is granted before running this test.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm revoke " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_FINE_LOCATION");
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm revoke " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_COARSE_LOCATION");
        }
    }

    @Test (timeout = 30000)
    public void testGreet() throws InterruptedException {

        try{
            revokeLocationPermission();
//            Thread.sleep(2000);
            Log.i("TEST", mActivityRule.getActivity().getmPermissionStatus()+"");
            assertTrue( mActivityRule.getActivity().getmPermissionStatus() == PERMISSION_GRANTED);
//            Log.i("TEST", "FALSE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}