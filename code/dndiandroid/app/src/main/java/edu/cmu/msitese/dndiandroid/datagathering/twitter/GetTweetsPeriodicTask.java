package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.bezirk.middleware.Bezirk;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.msitese.dndiandroid.Utils;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by Yu-Lun Tsai on 16/06/2017.
 */

public class GetTweetsPeriodicTask extends AsyncTask <Void, Void, Void> {

    private static final String TAG = "ZIRK";

    private Context mContext;
    private Bezirk mBezirk;
    private Twitter mTwitter;
    private String mScreenName;

    public GetTweetsPeriodicTask(Context context, Bezirk bezirk, Twitter twitter, String screenName){
        this.mContext = context;
        this.mBezirk = bezirk;
        this.mTwitter = twitter;
        this.mScreenName = screenName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        pullTweetsSinceLastID();
        return null;
    }

    // load the last id from the sharedPreference first, and set it as one of the REST argument
    // if there is anything new it will send a raw data event or it should print out no new tweets
    // in the console
    private void pullTweetsSinceLastID(){

        TwitterDAO dao = new TwitterDAO(mContext);
        long lastID = dao.loadLastTweetID();

        List<twitter4j.Status>  statuses;
        Paging page = new Paging(1, 100);

        // if the lastID is -1, there is no last id is stored in the sharedPreference
        if(lastID != -1){
            page.setSinceId(lastID);
        }

        try {
            statuses = mTwitter.getUserTimeline(mScreenName, page);
            if(!statuses.isEmpty()){

                final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.PERIODIC);
                Set<Long> set = new TreeSet<>();

                for(twitter4j.Status status: statuses){
                    set.add(status.getId());
                    event.appendRawData(Utils.packTweetToRawDataFormat(status));
                }

                event.hasText = true;
                event.hasLocation = true;
                mBezirk.sendEvent(event);

                long last = Collections.max(set);
                dao.saveLastTweetID(last);
            }
            else{
                Log.i(TAG, "There is no new tweet. The last tweet ID is: " + Long.toString(lastID));
            }
        } catch (TwitterException e) {
            Log.e(TAG, e.toString());
        }
    }
}
