package edu.cmu.msitese.dndiandroid;

import android.support.test.InstrumentationRegistry;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import org.junit.Test;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterInfoDao;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFramework;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Yu-Lun Tsai on 06/07/2017.
 */

public class DNDIFrameworkTest  {

    @Test(timeout = 30000)
    public void testDNDIFrameworkIntegration() throws InterruptedException {

        // clear twitter credential first
        TwitterInfoDao twitterInfoDao = new TwitterInfoDao(InstrumentationRegistry.getTargetContext());
        twitterInfoDao.clearTwitterCredential();

        // initialize the DNDIFramework
        DNDIFramework dndi = new DNDIFramework(InstrumentationRegistry.getTargetContext());
        Thread.sleep(4000);
        assertTrue(dndi.ready());

        Bezirk bezirk = BezirkMiddleware.registerZirk("TestZirk");
        final Object syncObject = new Object();
        final EventSet eventSet = new EventSet(
                RawDataEvent.class
        );

        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof RawDataEvent){
                    synchronized (syncObject){
                        syncObject.notify();
                    }
                }
            }
        });
        bezirk.subscribe(eventSet);

        // configure access token
        dndi.configTwitterCredential(
                BuildConfig.TWITTER_ACCESS_TOKEN,
                BuildConfig.TWITTER_ACCESS_SECRET,
                BuildConfig.TWITTER_USER_ID);
        Thread.sleep(1000);

        // test pull operation and the test Zirk must be notified
        dndi.pullTweetInBatchAll();
        synchronized (syncObject){
            syncObject.wait();
        }

        // test pull operation by num and the test Zirk must be notified
        dndi.pullTweetInBatchByNum(20);
        synchronized (syncObject){
            syncObject.wait();
        }

        // test pull operation by num and the test Zirk must be notified
        dndi.configTwitterPeriodicMode(500);
        synchronized (syncObject){
            syncObject.wait();
        }

        // configure the dndi into event mode
        dndi.configTwitterEventMode();

        // pause dndi and restart it
        dndi.pause();
        dndi.resume();

        // stop the dndi
        dndi.stop();
        Thread.sleep(4000);
        assertFalse(dndi.ready());

        // close the bezirk interface
        bezirk.unregisterZirk();
    }
>>>>>>> b212ed90b54b16d106cfb349f949c34ff3d02b86
}
