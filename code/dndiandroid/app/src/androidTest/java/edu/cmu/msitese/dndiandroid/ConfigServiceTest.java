package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import edu.cmu.msitese.dndiandroid.event.ResultEvent;
import edu.cmu.msitese.dndiandroid.frameworkinterface.ConfigService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class ConfigServiceTest extends ServiceTestCase<ConfigService> {

    public ConfigServiceTest() {
        super(ConfigService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testWithBoundService() throws TimeoutException {
        IBinder binder = bindService(new Intent(getContext(), ConfigService.class));
        ConfigService service = ((ConfigService.ConfigServiceBinder) binder).getService();
    }

    @Test
    public void testWithStartedService() throws TimeoutException {
        startService(new Intent(getContext(), ConfigService.class));
        //do nothing
    }

    @Test(timeout=10000)
    public void testSendBezirkEventDoesCallBezirkDotSend() throws Exception {

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), ConfigService.class));

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
    }

    @Test(timeout=10000)
    public void testWhetherEventReceiverIsCalledWhenSendAnEvent() throws Exception {

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), ConfigService.class));

        // Get the service handle
        ConfigService service = ((ConfigService.ConfigServiceBinder) binder).getService();
        assertNotNull(service);

        final Object syncObject = new Object();
        EventSet.EventReceiver old = service.getEventSetSubscriptionHandler();

        // set eventSet receiver handler
        service.setEventSetSubscriptionHandler(new EventSet.EventReceiver(){
            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
                if(event instanceof ResultEvent){
                    synchronized (syncObject){
                        syncObject.notify();
                    }
                }
            }
        });

        // send a bezirk message here
        Bezirk realBezirk = BezirkMiddleware.registerZirk("UnitTestZirk");
        final ResultEvent dummyEvent = new ResultEvent();
        realBezirk.sendEvent(dummyEvent);

        // wait for callback function
        synchronized (syncObject){
            syncObject.wait();
        }

        // unregister testing zirk
        realBezirk.unregisterZirk();
    }
}
