package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.datanormalization.location.GeocodingService;
import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;

/**
 * Created by rajatmathur on 8/13/17.
 */

public class GeocodingTest extends ServiceTestCase<GeocodingService> {

    public GeocodingTest() {
        super(GeocodingService.class);
    }

    @Test(timeout = 10000)
    public void testGeoCodingReceiveAddress() throws InterruptedException {
        //Initialize the Bezirk Middleware
        BezirkMiddleware.initialize(getContext());

        final Object syncObject = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                IBinder binder = bindService(new Intent(getContext(), GeocodingService.class));
                assertNotNull(binder);

                // Get service instances
                GeocodingService service = ((GeocodingService.GeocodingServiceBinder) binder).getService();
                assertNotNull(service);

                // send a bezirk message here
                Bezirk bezirk = BezirkMiddleware.registerZirk("GeoTest");

                try {
                    //Test the service by sending empty location data
                    RawData rawData = new RawData();
                    rawData.setLocation("{\"latitude\":\"" + "0" + "\",\"longitude\":\"" + "0" + "\"}");
                    RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
                    event.hasLocation = true;
                    event.appendRawData(rawData);
                    bezirk.sendEvent(event);
                    Thread.sleep(2000);
                    assertTrue(service.getLatLong() != null);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // close bezirk at the end
                BezirkMiddleware.stop();

                synchronized (syncObject) {
                    syncObject.notify();
                }
            }
        }, 1000);
        // wait for timertask to complete
        synchronized (syncObject) {
            syncObject.wait();
        }
    }
}

