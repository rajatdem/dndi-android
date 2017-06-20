package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import org.junit.Rule;
import org.junit.Test;

import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import edu.cmu.msitese.dndiandroid.frameworkinterface.ConfigService;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class ConfigServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test(timeout = 10000)
    public void testWithBoundService() throws Exception {

        Intent serviceIntent = new Intent(InstrumentationRegistry.getTargetContext(), ConfigService.class);

        mServiceRule.startService(
                new Intent(InstrumentationRegistry.getTargetContext(), ConfigService.class));

        // Bind the service and grab a reference to the binder.
        IBinder binder = mServiceRule.bindService(serviceIntent);

        // Get the service handle
        ConfigService service = ((ConfigService.ConfigServiceBinder) binder).getService();
        assertNotNull(service);

        // create mock objects
        Event mockEvent = mock(Event.class);
        Bezirk mockBezirk = mock(Bezirk.class);

        // create a bezirk spy to monitor sendEvent function
        doNothing().when(mockBezirk).sendEvent(mockEvent);
        Bezirk spyBezirk = spy(mockBezirk);
        service.setBezirkInstance(spyBezirk);

        // config.sendBezirkEvent should definitely call bezirk.sendEvent
        service.sendBezirkEvent(mockEvent);
        verify(spyBezirk, times(1)).sendEvent(mockEvent);

        final Object syncObject = new Object();
        EventSet.EventReceiver old = service.getEventSetSubscriptionHandler();

        // set eventSet receiver handler
        service.setEventSetSubscriptionHandler(new EventSet.EventReceiver(){
            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
                if(event instanceof RawDataEvent){
                    synchronized (syncObject){
                        syncObject.notify();
                    }
                }
            }
        });

        // send a bezirk message here
        Bezirk realBezirk = BezirkMiddleware.registerZirk("UnitTestZirk");
        final RawDataEvent dummyEvent = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        realBezirk.sendEvent(dummyEvent);

        // wait for callback function
        synchronized (syncObject){
            syncObject.wait();
        }

        // resume the original eventSet.receiver
        service.setEventSetSubscriptionHandler(old);
        realBezirk.sendEvent(dummyEvent);

        // unregister testing zirk
        realBezirk.unregisterZirk();

        // terminate child services
        service.stopChildrenService();

        BezirkMiddleware.stop();
    }
}
