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
import edu.cmu.msitese.dndiandroid.event.ExceptionEvent;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by Yu-Lun Tsai on 16/06/2017.
 */

class GetTweetsPeriodicTask extends AsyncTask <Void, Void, Void> {

    private static final String TAG = "ZIRK";

    private Context mContext;
    private Bezirk mBezirk;
    private Twitter mTwitter;
    private String mScreenName;

    GetTweetsPeriodicTask(Context context, Bezirk bezirk, Twitter twitter, String screenName){
        mContext = context;
        mBezirk = bezirk;
        mTwitter = twitter;
        mScreenName = screenName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            pullTweetsSinceLastId();
        }
        catch (TwitterException ex){
            final ExceptionEvent event = new ExceptionEvent(this.getClass().getName(), ex);
            mBezirk.sendEvent(event);
        }
        return null;
    }

    /**
     * Pull all new tweets since the last tweet id. It will load the last id from the
     * sharedPreference first and set it as one of REST API arguments. If there are something
     * new, it sends a RawDataEvent. Otherwise, it should print to console that no new data is
     * available.
     *
     * @throws TwitterException
     * @see    RawDataEvent
     */
    private void pullTweetsSinceLastId() throws TwitterException {

        TwitterDao dao = new TwitterDao(mContext);
        long lastId = dao.loadLastTweetId();

        List<twitter4j.Status>  statuses;
        Paging page = new Paging(1, 100);

        // if the lastId is -1, it means no last id is stored
        if(lastId != -1){
            page.setSinceId(lastId);
        }
        statuses = mTwitter.getUserTimeline(mScreenName, page);

        // it can be null when unit test uses mocks
        if(statuses == null){
            return;
        }

        // add raw data to RawDataEvent
        if (!statuses.isEmpty()) {

            final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.PERIODIC);
            Set<Long> set = new TreeSet<>();

            for (twitter4j.Status status : statuses) {
                set.add(status.getId());
                event.appendRawData(Utils.packTweetToRawDataFormat(status));
            }
            event.hasText = true;
//            event.hasLocation = true;
            mBezirk.sendEvent(event);

            // Update the last tweet id
            lastId = Collections.max(set);
            dao.saveLastTweetId(lastId);

        } else {
            Log.i(TAG, this.getClass().getName() +
                    ":: no new tweet. The last tweet id: " +
                    Long.toString(lastId));
        }
    }
}
