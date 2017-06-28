package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import edu.cmu.msitese.dndiandroid.BuildConfig;
import edu.cmu.msitese.dndiandroid.Utils;

import edu.cmu.msitese.dndiandroid.event.CommandEvent;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;

/**
 * Created by Yu-Lun Tsai on 30/03/2017.
 */

public class TwitterService extends Service {

    private static final String TAG = "ZIRK";

    // load consumer key and secret from the build configuration
    private static final String TWITTER_API_KEY = BuildConfig.TWITTER_API_KEY;
    private static final String TWITTER_API_SECRET = BuildConfig.TWITTER_API_SECRET;

    // handles for interfacing with bezirk framework and twitter
    private Bezirk bezirk;
    private Twitter twitter;
    private TwitterStream twitterStream;
    private boolean streamRunning = false;

    // a flag that records whether the access token has been loaded
    private boolean mTokenLoaded;
    private String mScreenName = "";

    // Twitter Zirk operation modes
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public enum Mode {PULL, EVENT, PERIODIC}
    private Mode mMode = Mode.PULL;

    // used for periodic mode
    private Handler mHandler;
    private int mPeriod = 30000;
    private Runnable mRunnablePullTask = new Runnable() {

        @Override
        public void run() {
            pullTweetsPeriodic();
            mHandler.postDelayed(mRunnablePullTask, mPeriod);
        }
    };

    // used only for testing
    private final IBinder mBinder = new TwitterServiceBinder();

    @Override
    public void onCreate(){

        // init the handler
        mHandler = new Handler();

        // init twitter api
        initTwitterInterface();
        mTokenLoaded = loadTwitterAccessToken();

        // register the Bezirk middleware interface
        bezirk = BezirkMiddleware.registerZirk("TwitterZirk");

        final EventSet eventSet = new EventSet(CommandEvent.class);
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                final CommandEvent commandEvent = (CommandEvent) event;
                CommandEvent.CmdType cmdType = commandEvent.type;

                Log.i(TAG, this.getClass().getName() + ":: received " + cmdType);
                if(!mTokenLoaded && cmdType != CommandEvent.CmdType.CMD_CONFIG_API_KEY){
                    Log.w(TAG, "Configure the Twitter credential first");
                    return;
                }

                // clear up configuration for the last state
                if(mMode == Mode.EVENT){
                    terminateTwitterStreaming();
                }
                else if(mMode == Mode.PERIODIC){
                    terminatePeriodicTask();
                }

                switch (cmdType) {
                    case CMD_CONFIG_API_KEY:
                        mTokenLoaded = configTwitterAccessToken(commandEvent.extra);
                        break;
                    case CMD_PERIODIC:
                        mMode = Mode.PERIODIC;
                        mPeriod = Integer.parseInt(commandEvent.extra);
                        startPeriodicTask();
                        break;
                    case CMD_PULL:
                        mMode = Mode.PULL;
                        pullTweetsAll();
                        break;
                    case CMD_EVENT:
                        mMode = Mode.EVENT;
                        registerTwitterStreaming();
                        break;
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

    // set the consumer API key to the twitter4j interface
    private void initTwitterInterface(){
        twitter = new TwitterFactory().getInstance();
        twitterStream = new TwitterStreamFactory().getInstance();
        twitter.setOAuthConsumer(TWITTER_API_KEY, TWITTER_API_SECRET);
        twitterStream.setOAuthConsumer(TWITTER_API_KEY, TWITTER_API_SECRET);
    }

    // load twitter user access token from sharedPreference
    private boolean loadTwitterAccessToken(){

        TwitterDAO dao = new TwitterDAO(this);

        TwitterCredential credential = dao.getTwitterCredential();
        String token = credential.token;
        String secret = credential.secret;
        String screenName = credential.screenName;

        if(screenName.equals("")){
            return false;
        }
        mScreenName = credential.screenName;
        twitter.setOAuthAccessToken(new AccessToken(token, secret));
        twitterStream.setOAuthAccessToken(new AccessToken(token, secret));
        return true;
    }

    // parse the user access token from the json file and store it into sharedPreferecne
    private boolean configTwitterAccessToken(String jsonStr){

        TwitterCredential credential = Utils.getTwitterCredentialFromJSONRaw(jsonStr);
        TwitterDAO dao = new TwitterDAO(this);
        dao.saveTwitterCredential(credential.token, credential.secret, credential.screenName);

        mScreenName = credential.screenName;
        if(mScreenName.equals("")){
            return false;
        }
        twitter.setOAuthAccessToken(new AccessToken(credential.token, credential.secret));
        twitterStream.setOAuthAccessToken(new AccessToken(credential.token, credential.secret));
        return true;
    }

    // start a timer that create an async tasks periodically to pull the latest tweet
    private void startPeriodicTask(){
        mHandler.postDelayed(mRunnablePullTask, mPeriod);
    }

    // terminate the periodic task if it exists
    private void terminatePeriodicTask(){
        mHandler.removeCallbacks(mRunnablePullTask);
    }

    // pull all the tweets in on operation
    private void pullTweetsAll(){ new GetTweetsInBatchTask(bezirk, twitter, mScreenName).execute(); }

    // specify the number of tweets as the first argument and create an async task to do it
    private void pullTweetsByNum(int num){ new GetTweetsInBatchTask(bezirk, twitter, mScreenName).execute(num); }

    // helper function for create a periodic async task
    private void pullTweetsPeriodic(){ new GetTweetsPeriodicTask(getBaseContext(), bezirk, twitter, mScreenName).execute(); }

    // callback functions that is callable by twitterStream listener
    public void sendTwitterEventNotification(Status status){
        final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.STREAMING);
        event.appendRawData(Utils.packTweetToRawDataFormat(status));
        Log.i(TAG, event.toString());
        bezirk.sendEvent(event);
    }

    // start twitter streaming api
    private void registerTwitterStreaming(){
        // TwitterStream.user() method internally creates a thread to call these listener methods
        final UserStreamListener listener = new TwitterStreamListener(this);
        twitterStream.addListener(listener);

        // TODO: fix the bug when switching on/off streaming mode
        if(!streamRunning){
            twitterStream.user();
            streamRunning = true;
        }
    }

    // termiante twitter streaming api
    private void terminateTwitterStreaming(){
        twitterStream.clearListeners();

        // TODO: fix the bug when switching on/off streaming mode
        // twitterStream.cleanUp();
    }

    // For testing purpose only
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public class TwitterServiceBinder extends Binder {
        public TwitterService getService() {
            return TwitterService.this;
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setBezirkInstance(Bezirk bezirk){
        this.bezirk = bezirk;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setTwitterInstance(Twitter twitter){
        this.twitter = twitter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public Twitter getTwitterInstance(){
        return twitter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public Mode getCurrentMode(){
        return mMode;
    }
}
