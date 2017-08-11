package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterInfoDao;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class TwitterServiceTest extends ServiceTestCase<TwitterService> {

    public TwitterServiceTest() {
        super(TwitterService.class);
    }

    @Test(timeout = 30000)
    public void testTwitterModeSelectionAndOperation() throws InterruptedException {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // sync object used to check whether timertask completes before the timeout budget
        final Object syncObject = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // Bind the service and grab a reference to the binder.
                IBinder binder = bindService(new Intent(getContext(), TwitterService.class));
                assertNotNull(binder);

                // Get service instances
                final TwitterService service = ((TwitterService.TwitterServiceBinder) binder).getService();
                assertNotNull(service);

                // The default operation mode should be batch
                TwitterService.Mode mode = service.getCurrentMode();
                assertTrue(mode == TwitterService.Mode.PULL);

                // send a bezirk message here
                Bezirk bezirk = BezirkMiddleware.registerZirk("TestZirk");

                // configure twitter credential
                JSONObject jsonObject = Utils.packCredentialToJSON(
                        BuildConfig.TWITTER_ACCESS_TOKEN,
                        BuildConfig.TWITTER_ACCESS_SECRET,
                        BuildConfig.TWITTER_USER_ID);
                final CommandEvent event1 = new CommandEvent(
                        getContext().getString(R.string.target_twitter),
                        CommandEvent.CmdType.CMD_CONFIG_API_KEY,
                        jsonObject.toString());
                bezirk.sendEvent(event1);

                // assert api key is successfully saved
                try {

                    // check whether credential is set
                    Thread.sleep(1000);
                    assertTrue(service.getTokenLoaded());

                    // assert mode change is successful
                    final CommandEvent event2 = new CommandEvent(
                            getContext().getString(R.string.target_twitter),
                            CommandEvent.CmdType.CMD_EVENT);
                    bezirk.sendEvent(event2);

                    // check whether twitter service successfully change mode
                    Thread.sleep(1000);
                    assertTrue(service.getCurrentMode() == TwitterService.Mode.EVENT);

                    /* Test Batch Operation */
                    // create twitter mock
                    Twitter mockTwitter = mock(Twitter.class);
                    when(mockTwitter.getUserTimeline(any(String.class), any(Paging.class))).thenReturn(null);
                    Twitter spyTwitter = spy(mockTwitter);

                    // swap the twitter instance in TwitterService
                    Twitter origTwitter = service.getTwitterInstance();
                    service.setTwitterInstance(spyTwitter);

                    // pull tweets in one period: twitter.getUserTimeline should be called at least once
                    final CommandEvent event3 = new CommandEvent(
                            getContext().getString(R.string.target_twitter),
                            CommandEvent.CmdType.CMD_PERIODIC,
                            "1500");
                    bezirk.sendEvent(event3);

                    Thread.sleep(2500);
                    assertTrue(service.getCurrentMode() == TwitterService.Mode.PERIODIC);
                    verify(spyTwitter, times(1)).getUserTimeline(any(String.class), any(Paging.class));

                    // pull all tweets: twitter.getUserTimeline should be called at least once
                    final CommandEvent event4 = new CommandEvent(
                            getContext().getString(R.string.target_twitter),
                            CommandEvent.CmdType.CMD_PULL);
                    bezirk.sendEvent(event4);

                    Thread.sleep(1000);
                    assertTrue(service.getCurrentMode() == TwitterService.Mode.PULL);
                    verify(spyTwitter, times(2)).getUserTimeline(any(String.class), any(Paging.class));

                    // pull all tweets: twitter.getUserTimeline should be called at least once
                    final CommandEvent event5 = new CommandEvent(
                            getContext().getString(R.string.target_twitter),
                            CommandEvent.CmdType.CMD_PULL,
                            "20");
                    bezirk.sendEvent(event5);

                    Thread.sleep(1000);
                    assertTrue(service.getCurrentMode() == TwitterService.Mode.PULL);
                    verify(spyTwitter, times(3)).getUserTimeline(any(String.class), any(Paging.class));

                    // resume original setting
                    service.setTwitterInstance(origTwitter);

                    bezirk.unregisterZirk();

                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }

                // close bezirk at the end
                BezirkMiddleware.stop();

                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        }, 1000);
        // wait for timertask to complete
        synchronized (syncObject){
            syncObject.wait();
        }
    }

    @Test(timeout=10000)
    public void testWhetherEventReceiverIsCalledWhenSendAnEvent() throws InterruptedException {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // sync object used to check whether timertask completes before the timeout budget
        final Object syncObject1 = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // Bind the service and grab a reference to the binder.
                IBinder binder = bindService(new Intent(getContext(), TwitterService.class));

                // Get the service handle
                TwitterService service = ((TwitterService.TwitterServiceBinder) binder).getService();
                assertNotNull(service);

                final Object syncObject2 = new Object();
                EventSet.EventReceiver oldReceiver = service.getEventSetSubscriptionHandler();

                // set eventSet receiver handler
                service.setEventSetSubscriptionHandler(new EventSet.EventReceiver() {
                    @Override
                    public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
                        if (event instanceof CommandEvent) {
                            synchronized (syncObject2) {
                                syncObject2.notify();
                            }
                        }
                    }
                });

                // send a bezirk message here
                Bezirk realBezirk = BezirkMiddleware.registerZirk("TestZirk");
                final CommandEvent dummyEvent = new CommandEvent(
                        getContext().getString(R.string.target_twitter),
                        CommandEvent.CmdType.CMD_EVENT);
                realBezirk.sendEvent(dummyEvent);

                try{
                    // wait for callback function
                    synchronized (syncObject2) {
                        syncObject2.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                service.setEventSetSubscriptionHandler(oldReceiver);

                // unregister testing zirk
                realBezirk.unregisterZirk();

                // close bezirk at the end
                BezirkMiddleware.stop();

                synchronized (syncObject1) {
                    syncObject1.notify();
                }
            }
        }, 1000);

        // wait for timertask to complete
        synchronized (syncObject1){
            syncObject1.wait();
        }
    }

    @Test(timeout=10000)
    public void testSendTwitterEventNotificationDoesCallBezirkSend() throws InterruptedException {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // sync object used to check whether timertask completes before the timeout budget
        final Object syncObject = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // Bind the service and grab a reference to the binder.
                IBinder binder = bindService(new Intent(getContext(), TwitterService.class));

                // Get the service handle
                TwitterService service = ((TwitterService.TwitterServiceBinder) binder).getService();
                assertNotNull(service);

                // create mock objects
                Status mockStatus = mock(Status.class);
                Bezirk mockBezirk = mock(Bezirk.class);

                // create a bezirk spy to monitor sendEvent function
                doNothing().when(mockBezirk).sendEvent(any(Event.class));
                when(mockStatus.getCreatedAt()).thenReturn(new Date());
                Bezirk spyBezirk = spy(mockBezirk);
                service.setBezirkInstance(spyBezirk);

                // config.sendBezirkEvent should definitely call bezirk.sendEvent
                service.sendTwitterEventNotification(mockStatus);
                verify(spyBezirk, times(1)).sendEvent(any(Event.class));

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

    @Test
    public void testTwitterWithoutCredentialExpectTimeout() throws InterruptedException {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // sync object used to check whether timertask completes before the timeout budget
        final Object syncObject = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                // clear any credential in the persistent storage
                TwitterInfoDao twitterInfoDao = new TwitterInfoDao(getContext());
                twitterInfoDao.clearTwitterCredential();

                // Bind the service and grab a reference to the binder.
                IBinder binder = bindService(new Intent(getContext(), TwitterService.class));
                assertNotNull(binder);

                // Get service instances
                TwitterService service = ((TwitterService.TwitterServiceBinder) binder).getService();
                assertNotNull(service);

                // The default operation mode should be batch
                TwitterService.Mode mode = service.getCurrentMode();
                assertTrue(mode == TwitterService.Mode.PULL);

                // assert the dao successfully clear token
                assertFalse(service.getTokenLoaded());

                // send a mode change event
                Bezirk bezirk = BezirkMiddleware.registerZirk("TestZirk");
                final CommandEvent event = new CommandEvent(CommandEvent.CmdType.CMD_EVENT);
                bezirk.sendEvent(event);

                int count = 0;
                boolean check = false;
                try{
                    while (service.getCurrentMode() != TwitterService.Mode.EVENT) {
                        Thread.sleep(500);
                        count++;
                        if (count >= 10) {
                            check = true;
                            break;
                        }
                    }
                }
                catch (Exception e){
                    fail();
                }
                assertTrue(check);

                // close bezirk at the end
                bezirk.unregisterZirk();
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
