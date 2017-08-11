package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;

import static android.R.attr.mode;

/**
 * Created by rajatmathur on 8/3/17.
 */

public class GPSServiceTest extends ServiceTestCase<LocationDataService>{

    public GPSServiceTest() {
        super(LocationDataService.class);
    }

    @Test(timeout = 30000)
    public void testGPSOperationAndModeSelection () throws InterruptedException {

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
                    // assert mode change is successful
                    final CommandEvent event2 = new CommandEvent(
                            getContext().getString(R.string.target_gps),
                            CommandEvent.CmdType.CMD_EVENT);
                    bezirk.sendEvent(event2);
                    Thread.sleep(3000);
                    Log.i("Test", service.getCurrentMode());
                    assertTrue(service.getCurrentMode().equals("EVENT"));
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
}
