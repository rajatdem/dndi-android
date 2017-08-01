package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import edu.cmu.msitese.dndiandroid.BuildConfig;
import edu.cmu.msitese.dndiandroid.R;
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

    // handles for interfacing with mBezirk framework and mTwitter
    private Bezirk mBezirk;
    private final EventSet eventSet = new EventSet(
            CommandEvent.class
    );
    private Twitter mTwitter;
    private TwitterStream mTwitterStream;
    private boolean mStreamIsRunning = false;

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
        mHandler = new Handler(Looper.getMainLooper());

        // init mTwitter api
        initTwitterInterface();
        mTokenLoaded = loadTwitterAccessToken();

        // register the Bezirk middleware interface
        mBezirk = BezirkMiddleware.registerZirk("TwitterZirk");
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {

                final CommandEvent commandEvent = (CommandEvent) event;
                CommandEvent.CmdType cmdType = commandEvent.type;

                // check whether the command is targeted at twitter zirk
                if(!commandEvent.target.equals(getString(R.string.target_twitter))){
                    return;
                }

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
                    case CMD_CONFIG_API_KEY: {
                        mTokenLoaded = configTwitterAccessToken(commandEvent.extra);
                        break;
                    }
                    case CMD_PERIODIC:{
                        int period;

                        mMode = Mode.PERIODIC;
                        try {
                            period = Integer.parseInt(commandEvent.extra);
                            mPeriod = period;
                        }
                        // if the input string data is invalid, use the last configuration
                        catch (NumberFormatException e){}
                        startPeriodicTask();
                        break;
                    }
                    case CMD_PULL:{
                        boolean validNum = false;

                        mMode = Mode.PULL;
                        if(commandEvent.extra != null && !commandEvent.extra.isEmpty()){
                            int num;
                            try {
                                num = Integer.parseInt(commandEvent.extra);
                                validNum = true;
                                pullTweetsByNum(num);
                            }
                            // if the input string data is invalid, pull all tweets
                            catch (NumberFormatException e){}
                        }
                        if(!validNum){
                            pullTweetsAll();
                        }
                        break;
                    }
                    case CMD_EVENT:
                        mMode = Mode.EVENT;
                        registerTwitterStreaming();
                        break;
                }
            }
        });
        mBezirk.subscribe(eventSet);
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
        mTwitter = new TwitterFactory().getInstance();
        mTwitterStream = new TwitterStreamFactory().getInstance();
        mTwitter.setOAuthConsumer(TWITTER_API_KEY, TWITTER_API_SECRET);
        mTwitterStream.setOAuthConsumer(TWITTER_API_KEY, TWITTER_API_SECRET);
    }

    // load mTwitter user access token from sharedPreference
    private boolean loadTwitterAccessToken(){

        TwitterDao dao = new TwitterDao(this);

        TwitterCredential credential = dao.getTwitterCredential();
        String token = credential.token;
        String secret = credential.secret;
        String screenName = credential.screenName;

        if(screenName.equals("")){
            return false;
        }
        mScreenName = credential.screenName;
        mTwitter.setOAuthAccessToken(new AccessToken(token, secret));
        mTwitterStream.setOAuthAccessToken(new AccessToken(token, secret));
        return true;
    }

    // parse the user access token from the json file and store it into sharedPreferecne
    private boolean configTwitterAccessToken(String jsonStr){

        TwitterCredential credential = Utils.getTwitterCredentialFromJSONRaw(jsonStr);
        TwitterDao dao = new TwitterDao(this);
        dao.saveTwitterCredential(credential.token, credential.secret, credential.screenName);

        mScreenName = credential.screenName;
        if(mScreenName.equals("")){
            return false;
        }
        mTwitter.setOAuthAccessToken(new AccessToken(credential.token, credential.secret));
        mTwitterStream.setOAuthAccessToken(new AccessToken(credential.token, credential.secret));
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
    private void pullTweetsAll(){ new GetTweetsInBatchTask(mBezirk, mTwitter, mScreenName).execute(); }

    // specify the number of tweets as the first argument and create an async task to do it
    private void pullTweetsByNum(int num){ new GetTweetsInBatchTask(mBezirk, mTwitter, mScreenName).execute(num); }

    // helper function for create a periodic async task
    private void pullTweetsPeriodic(){ new GetTweetsPeriodicTask(getBaseContext(), mBezirk, mTwitter, mScreenName).execute(); }

    // callback functions that is callable by mTwitterStream listener
    public void sendTwitterEventNotification(Status status){
        final RawDataEvent event = new RawDataEvent(RawDataEvent.GatherMode.STREAMING);
        event.hasText = true;
        event.appendRawData(Utils.packTweetToRawDataFormat(status));
        mBezirk.sendEvent(event);
    }

    // start mTwitter streaming api
    private void registerTwitterStreaming(){
        // TwitterStream.user() method internally creates a thread to call these listener methods
        final UserStreamListener listener = new TwitterStreamListener(this);
        mTwitterStream.addListener(listener);

        // TODO: fix the bug when switching on/off streaming mode
        if(!mStreamIsRunning){
            mTwitterStream.user();
            mStreamIsRunning = true;
        }
    }

    // termiante mTwitter streaming api
    private void terminateTwitterStreaming(){
        mTwitterStream.clearListeners();

        // TODO: fix the bug when switching on/off streaming mode
        // mTwitterStream.cleanUp();
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
        mBezirk = bezirk;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setTwitterInstance(Twitter twitter){
        mTwitter = twitter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public Twitter getTwitterInstance(){
        return mTwitter;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public boolean getTokenLoaded(){
        return mTokenLoaded;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setEventSetSubscriptionHandler(EventSet.EventReceiver receiver){
        eventSet.setEventReceiver(receiver);
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public EventSet.EventReceiver getEventSetSubscriptionHandler(){
        return eventSet.getEventReceiver();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public Mode getCurrentMode(){
        return mMode;
    }
}
