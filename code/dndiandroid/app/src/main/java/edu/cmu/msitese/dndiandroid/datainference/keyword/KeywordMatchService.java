package edu.cmu.msitese.dndiandroid.datainference.keyword;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.msitese.dndiandroid.event.KeywordMatchEvent;
import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;

/**
 * Created by Yu-Lun Tsai on 19/06/2017.
 */

public class KeywordMatchService extends Service {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;
    private final EventSet eventSet = new EventSet(
            RawDataEvent.class
    );

    // key: keywords, value: categories
    private Map<String,String> mKeywordMap;
    private KeywordCountDao mKeywordCountDao;

    // used only for testing
    private final IBinder mBinder = new KeywordMatchServiceBinder();

    @Override
    public void onCreate(){

        // load keywords from android resources first when onCreate
        KeywordFileLoader dao = new KeywordFileLoader(getBaseContext());
        mKeywordMap = dao.loadKeywords();

        // instantiate the keyword dao
        mKeywordCountDao = new KeywordCountDao(this);

        // register itself to the Bezirk middleware
        bezirk = BezirkMiddleware.registerZirk("KeywordZirk");
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                if(event instanceof RawDataEvent){
                    final RawDataEvent rawDataEvent = (RawDataEvent) event;
                    if(rawDataEvent.hasText){
                        Log.i(TAG, this.getClass().getName() + ":: received raw text..." + rawDataEvent.toString());
                        checkKeywordMatch(rawDataEvent.getRawDataArray());
                    }
                }
            }
        });
        bezirk.subscribe(eventSet);

        Log.i(TAG, this.getClass().getName() + ":: wait...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // given a list of RawData objects, check whether there is keywords in the text
    private void checkKeywordMatch(List<RawData> array){

        final KeywordMatchEvent keywordMatchEvent = new KeywordMatchEvent();
        List<Keyword> keywordList = new ArrayList<>();

        // split the text into tokens and check whether these tokens are in the hash map
        // if there is a match, it will append the match category at the end of the keywordMatchEvent
        // send it back to the configService
        for(RawData data: array){
            String[] tokens = data.getText().toLowerCase().split("\\s+");
            for(String token: tokens){
                if(mKeywordMap.containsKey(token)){
                    String category = mKeywordMap.get(token);
                    keywordMatchEvent.increaseMatchOccurrence(category);
                    keywordList.add(new Keyword(token, category));
                    Log.i(TAG, this.getClass().getName() + ":: match " + token + " (" + category + ")");
                }
            }
        }
        if(keywordMatchEvent.hasMatch()){
            bezirk.sendEvent(keywordMatchEvent);
        }
        if(!keywordList.isEmpty()){
            new UpdateKeywordCountTask().execute(
                    (Keyword[])keywordList.toArray(new Keyword[keywordList.size()])
            );
        }
    }

    private class UpdateKeywordCountTask extends AsyncTask<Keyword,Void,Void>{

        @Override
        protected Void doInBackground(Keyword... keywords) {
            for (Keyword keyword : keywords) {
                mKeywordCountDao.addOrUpdateKeywordCount(keyword.keyword, keyword.category);
            }
            return null;
        }
    }

    // For testing purpose only
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public class KeywordMatchServiceBinder extends Binder {
        public KeywordMatchService getService() {
            return KeywordMatchService.this;
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setKeywordCountDaoInstance(KeywordCountDao dao){
        mKeywordCountDao = dao;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public KeywordCountDao getKeywordCountDaoInstance(){
        return mKeywordCountDao;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void insertKeywordCategoryPair(String keyword, String category){
        mKeywordMap.put(keyword, category);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public String getKeywordCategory(String keyword){
        if(mKeywordMap.containsKey(keyword)){
            return mKeywordMap.get(keyword);
        }
        return null;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void resumeKeywordCategoryPair(String keyword, String category){
        if(category == null){
            if(mKeywordMap.containsKey(keyword)){
                mKeywordMap.remove(keyword);
            }
        }
        else{
            mKeywordMap.put(keyword, category);
        }
    }
}
