package edu.cmu.msitese.dndiandroid;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.android.BezirkMiddleware;

import org.junit.Test;

import edu.cmu.msitese.dndiandroid.datainference.keyword.KeywordCountDao;
import edu.cmu.msitese.dndiandroid.datainference.keyword.KeywordMatchService;
import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Yu-Lun Tsai on 06/07/2017.
 */

public class KeywordMatchServiceTest extends ServiceTestCase<KeywordMatchService> {

    public KeywordMatchServiceTest() {
        super(KeywordMatchService.class);
    }

    @Test(timeout = 30000)
    public void testTwitterModeSelectionAndOperation() throws InterruptedException {

        // initialize the Bezirk service for testing
        BezirkMiddleware.initialize(getContext());

        // Bind the service and grab a reference to the binder.
        IBinder binder = bindService(new Intent(getContext(), KeywordMatchService.class));
        assertNotNull(binder);

        // Get service instances
        KeywordMatchService service = ((KeywordMatchService.KeywordMatchServiceBinder) binder).getService();
        assertNotNull(service);

        // perform dependency injection to replace original dao
        KeywordCountDao origDao = service.getKeywordCountDaoInstance();
        KeywordCountDao mockDao = mock(KeywordCountDao.class);
        doNothing().when(mockDao).addOrUpdateKeywordCount(any(String.class), any(String.class));

        KeywordCountDao spyDao = spy(mockDao);
        service.setKeywordCountDaoInstance(spyDao);

        // create a set of testing keyword and category
        String fakeKeyword = getClass().getName();
        String fakeCategory = getClass().getSimpleName();
        String origCategory = service.getKeywordCategory(fakeKeyword);
        service.insertKeywordCategoryPair(fakeKeyword, fakeCategory);

        String fakeTweet = "test a keyword " + fakeKeyword;
        final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);
        RawData data = new RawData();
        data.setText(fakeTweet);
        event.appendRawData(data);

        Bezirk bezirk = BezirkMiddleware.registerZirk("TestZirk");
        bezirk.sendEvent(event);

        // there must be a keyword matcc, which will call  dao.addOrUpdateKeywordCount()
        Thread.sleep(2000);
        verify(spyDao, times(1)).addOrUpdateKeywordCount(any(String.class), any(String.class));

        // resume the original keyword dao object
        service.setKeywordCountDaoInstance(origDao);

        // resume keyword map in KeywordMatchService
        service.resumeKeywordCategoryPair(fakeKeyword, origCategory);

        // close bezirk at the end
        bezirk.unregisterZirk();
        BezirkMiddleware.stop();
    }
}
