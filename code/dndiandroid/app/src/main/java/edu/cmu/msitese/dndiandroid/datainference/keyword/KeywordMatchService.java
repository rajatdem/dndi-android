package edu.cmu.msitese.dndiandroid.datainference.keyword;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import edu.cmu.msitese.dndiandroid.event.ResultEvent;

/**
 * Created by Yu-Lun Tsai on 19/06/2017.
 */

public class KeywordMatchService extends Service {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;
    private final EventSet eventSet = new EventSet(
            RawDataEvent.class
    );

    private Map<String,String> mKeywordMap;
    private KeywordCountDao mKeywordCountDao;

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
                    Log.i(TAG, this.getClass().getName() + ":: received raw..." + rawDataEvent.toString());
                    checkKeywordMatch(rawDataEvent.getRawDataArray());
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
        return null;
    }

    // given a list of RawData objects, check whether there is keywords in the text
    private void checkKeywordMatch(List<RawData> array){

        final ResultEvent resultEvent = new ResultEvent();
        List<Keyword> keywordList = new ArrayList<>();

        // split the text into tokens and check whether these tokens are in the hash map
        // if there is a match, it will append the match category at the end of the resultEvent
        // send it back to the configService
        for(RawData data: array){
            String[] tokens = data.getText().split("\\s+");
            for(String token: tokens){
                if(mKeywordMap.containsKey(token)){
                    String category = mKeywordMap.get(token);
                    resultEvent.increaseMatchOccurrence(category);
                    keywordList.add(new Keyword(token, category));
                    Log.i(TAG, this.getClass().getName() + ":: match " + token + " (" + category + ")");
                }
            }
        }
        if(resultEvent.hasMatch()){
            bezirk.sendEvent(resultEvent);
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
}
