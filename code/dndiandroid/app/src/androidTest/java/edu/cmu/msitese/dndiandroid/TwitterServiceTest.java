package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Test;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;


/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class TwitterServiceTest extends ServiceTestCase<TwitterService> {

    public TwitterServiceTest() {
        super(TwitterService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test(timeout = 10000)
    public void testWithBoundService() throws Exception {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), TwitterService.class));
        assertNotNull(binder);

        // Get service instances
        TwitterService service = ((TwitterService.TwitterServiceBinder) binder).getService();
        assertNotNull(service);

        // The default operation mode should be batch
        TwitterService.Mode mode = service.getCurrentMode();
        assertTrue(mode == TwitterService.Mode.PULL);

        // close bezirk at the end
        BezirkMiddleware.stop();
    }
}
