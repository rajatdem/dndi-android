package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.os.AsyncTask;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import edu.cmu.msitese.dndiandroid.Utils;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by Yu-Lun Tsai on 16/06/2017.
 */

public class GetTweetsInBatchTask extends AsyncTask <Integer, Void, Void> {

    private static final String TAG = "ZIRK";

    private Bezirk bezirk;
    private Twitter mTwitter;
    private String mUserScreenName;

    private List<twitter4j.Status> statuses = new ArrayList<>();

    public GetTweetsInBatchTask(Bezirk bezirk, Twitter twitter, String screenName){
        this.bezirk = bezirk;
        this.mTwitter = twitter;
        this.mUserScreenName = screenName;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.BATCH);

        // get as many tweets as possible if no number is specified
        if(params.length == 0){
            int nextPageNum = 1;
            int maxPageNum = 16;

            // pull the first 1600 tweets first
            nextPageNum = pullTweetsByPageRange(nextPageNum, maxPageNum);

            // pull the rest of tweets if there are more than 1600
            if(nextPageNum > maxPageNum){
                maxPageNum = 32;
                statuses.clear();
                pullTweetsByPageRange(nextPageNum, maxPageNum);
            }
        }
        else{
            int num = params[0];
            if(num > 0){
                pullTweetsByNum(num);
            }
        }

        if(!statuses.isEmpty()){
            for (twitter4j.Status status : statuses) {
                event.appendRawData(Utils.packTweetToRawDataFormat(status));
            }
            event.hasText = true;
            event.hasLocation = true;
            bezirk.sendEvent(event);
        }
        return null;
    }

    private int pullTweetsByPageRange(int start, int end){

        while(start <= end) {
            try {
                int size = statuses.size();
                Paging page = new Paging(start++, 100);
                statuses.addAll(mTwitter.getUserTimeline(mUserScreenName, page));
                if (statuses.size() == size){
                    break;
                }
            }
            catch(TwitterException e) {
                Log.e(TAG, e.toString());
            }
        }
        return start;
    }

    private boolean pullTweetsByNum(int num){

        int totalPageNum = num/100;
        int remain = num%100;
        totalPageNum = (remain > 0) ? totalPageNum + 1 : totalPageNum;

        int nextPageNum = 1;
        int tweetsPerPage = (nextPageNum == totalPageNum) ? ((remain == 0) ? 100 : remain): 100;

        while(true) {
            try {
                int size = statuses.size();
                Paging page = new Paging(nextPageNum, tweetsPerPage);
                statuses.addAll(mTwitter.getUserTimeline(mUserScreenName, page));
                if (statuses.size() == size || statuses.size() >= num){
                    break;
                }
                nextPageNum++;
                tweetsPerPage = (nextPageNum == totalPageNum) ? ((remain == 0) ? 100 : remain): 100;
            }
            catch(TwitterException e) {
                Log.e(TAG, e.toString());
            }
        }
        return (statuses.size() == num);
    }
}
