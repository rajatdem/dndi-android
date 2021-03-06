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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import edu.cmu.msitese.dndiandroid.event.KeywordMatchEvent;
import edu.cmu.msitese.dndiandroid.frameworkinterface.ZirkManagerService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class ZirkManagerServiceTest extends ServiceTestCase<ZirkManagerService> {

    public ZirkManagerServiceTest() {
        super(ZirkManagerService.class);
    }

    @Test
    public void testWithBoundService() throws TimeoutException {
        IBinder binder = bindService(new Intent(getContext(), ZirkManagerService.class));
        ZirkManagerService service = ((ZirkManagerService.ZirkManagerServiceBinder) binder).getService();
        assertNotNull(service);
    }

    @Test
    public void testWithStartedService() throws TimeoutException {
        startService(new Intent(getContext(), ZirkManagerService.class));
    }

    @Test(timeout=10000)
    public void testSendBezirkEventDoesCallBezirkSend() throws Exception {

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), ZirkManagerService.class));

        // Get the service handle
        ZirkManagerService service = ((ZirkManagerService.ZirkManagerServiceBinder) binder).getService();
        assertNotNull(service);

        // create mock objects
        Event mockEvent = mock(Event.class);
        Bezirk mockBezirk = mock(Bezirk.class);

        // create a bezirk spy to monitor sendEvent function
        doNothing().when(mockBezirk).sendEvent(mockEvent);
        Bezirk spyBezirk = spy(mockBezirk);
        service.setBezirkInstance(spyBezirk);

        // sendBezirkEvent should definitely call bezirk.sendEvent
        service.sendBezirkEvent(mockEvent);
        verify(spyBezirk, times(1)).sendEvent(mockEvent);
    }

    @Test(timeout=10000)
    public void testWhetherEventReceiverIsCalledWhenSendAnEvent() throws Exception {

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), ZirkManagerService.class));

        // Get the service handle
        final ZirkManagerService service = ((ZirkManagerService.ZirkManagerServiceBinder) binder).getService();
        assertNotNull(service);

        final Object syncObject1 = new Object();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                EventSet.EventReceiver old = service.getEventSetSubscriptionHandler();

                final Object syncObject2 = new Object();

                // set eventSet receiver handler
                service.setEventSetSubscriptionHandler(new EventSet.EventReceiver(){
                    @Override
                    public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
                        if(event instanceof KeywordMatchEvent){
                            synchronized (syncObject2){
                                syncObject2.notify();
                            }
                        }
                    }
                });

                // send a bezirk message here
                Bezirk realBezirk = BezirkMiddleware.registerZirk("TestZirk");
                final KeywordMatchEvent dummyEvent = new KeywordMatchEvent();
                realBezirk.sendEvent(dummyEvent);

                try {
                    // wait for callback function
                    synchronized (syncObject2) {
                        syncObject2.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail();
                }
                service.setEventSetSubscriptionHandler(old);

                // unregister testing zirk
                realBezirk.unregisterZirk();

                synchronized (syncObject1) {
                    syncObject1.notify();
                }
            }
        }, 2000);

        // wait for timertask to complete
        synchronized (syncObject1) {
            syncObject1.wait();
        }
    }
}
