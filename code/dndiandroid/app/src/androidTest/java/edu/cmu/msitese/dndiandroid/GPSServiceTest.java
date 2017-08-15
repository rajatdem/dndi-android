package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import org.junit.runners.model.Statement;

import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;

import static android.R.attr.mode;
import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Created by rajatmathur on 8/3/17.
 */

public class GPSServiceTest extends ServiceTestCase<LocationDataService>{

    public GPSServiceTest() {
        super(LocationDataService.class);
    }

    public void grantLocationPermission() {
        // In M+, trying to call a number will trigger a runtime dialog. Make sure
        // the permission is granted before running this test.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_FINE_LOCATION");
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_COARSE_LOCATION");
        }
    }


    @Test(timeout = 30000)
    public void testGPSOperationAndModeSelection () throws InterruptedException {
        grantLocationPermission();

        //Initialize the Bezirk Middleware
        BezirkMiddleware.initialize(getContext());

        final Object syncObject = new Object();

        new Timer().schedule( new TimerTask() {
            @Override
            public void run() {
                IBinder binder = bindService(new Intent(getContext(), LocationDataService.class));
                assertNotNull(binder);

                // Get service instances
                LocationDataService service = ((LocationDataService.LocationDataServiceBinder) binder).getService();
                assertNotNull(service);

                // The default operation mode should be none
                LocationDataService.setMode(service.getCurrentMode());
                assertTrue(LocationDataService.getCurrentMode().equals("NONE"));

                // send a bezirk message here
                Bezirk bezirk = BezirkMiddleware.registerZirk("TestGPSZirk");

                try {
                    // event mode change is successful
                    final CommandEvent event_1 = new CommandEvent(
                            getContext().getString(R.string.target_gps),
                            CommandEvent.CmdType.CMD_EVENT);
                    bezirk.sendEvent(event_1);
                    Thread.sleep(3000);
                    assertTrue(service.getCurrentMode().equals("EVENT"));

                    //assert pull mode is settable
                    final CommandEvent event_2 = new CommandEvent(
                            getContext().getString(R.string.target_gps),
                            CommandEvent.CmdType.CMD_PULL);
                    bezirk.sendEvent(event_2);
                    Thread.sleep(3000);
                    assertTrue(service.getCurrentMode().equals("BATCH"));

                    //assert can set into periodic mode
                    final CommandEvent event_3 = new CommandEvent(
                            getContext().getString(R.string.target_gps),
                            CommandEvent.CmdType.CMD_PERIODIC);
                    bezirk.sendEvent(event_3);
                    Thread.sleep(3000);
                    assertTrue(service.getCurrentMode().equals("PERIODIC"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail();
                }
                // close bezirk at the end
                BezirkMiddleware.stop();

                synchronized (syncObject) {
                    syncObject.notify();
                }
            }
        }, 1000);
        // wait for timertask to complete
        synchronized (syncObject){
            syncObject.wait();
        }
    }

    @Test(timeout = 30000)
    public void testLocationActivity () {

    }
}
