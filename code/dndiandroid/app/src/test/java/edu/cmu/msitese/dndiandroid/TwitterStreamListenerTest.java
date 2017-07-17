package edu.cmu.msitese.dndiandroid;

import org.junit.Test;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterService;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterStreamListener;
import twitter4j.Status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Yu-Lun Tsai on 14/06/2017.
 */

public class TwitterStreamListenerTest {

    @Test
    public void testWhetherSendTwitterEventNotificationIsCalled(){

        TwitterService service = mock(TwitterService.class);
        Status status = mock(Status.class);

        TwitterStreamListener listener = spy(new TwitterStreamListener(service));
        listener.onStatus(status);

        verify(service, times(1)).sendTwitterEventNotification(any(Status.class));
    }
}
