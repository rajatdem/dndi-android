package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cmu.msitese.dndiandroid.R;

/**
 * Created by Yu-Lun Tsai on 08/06/2017.
 */

public class TwitterInfoDao {

    private static final String TAG = "ZIRK";

    private Context mContext;

    public TwitterInfoDao(Context context){
        mContext = context;
    }

    // save the specified token and secret into sharedPreference
    public void saveTwitterCredential(String token, String secret, String id){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.twitter_access_token), token);
        editor.putString(mContext.getString(R.string.twitter_access_secret), secret);
        editor.putString(mContext.getString(R.string.twitter_screen_name), id);
        editor.commit();
    }

    // load twitter access token from sharedPreference
    public TwitterCredential getTwitterCredential(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String token = sharedPref.getString(mContext.getString(R.string.twitter_access_token), "");
        String secret = sharedPref.getString(mContext.getString(R.string.twitter_access_secret), "");
        String id = sharedPref.getString(mContext.getString(R.string.twitter_screen_name), "");
        return new TwitterCredential(token, secret, id);
    }

    // clear twitter access token in the sharedPreference (for demo purpose only)
    public void clearTwitterCredential(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(mContext.getString(R.string.twitter_screen_name));
        editor.remove(mContext.getString(R.string.twitter_access_token));
        editor.remove(mContext.getString(R.string.twitter_access_secret));
        editor.apply();
    }

    // save the last tweet ID so that the periodic mode won't send out duplicate data
    public void saveLastTweetId(long id){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.twitter_last_tweet_id), Long.toString(id));
        editor.apply();
    }

    // load the last tweet ID
    public long loadLastTweetId(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String res = sharedPref.getString(mContext.getString(R.string.twitter_last_tweet_id), "");

        if(res.equals(""))
            return -1;
        return Long.parseLong(res);
    }

    // clear twitter access token in the sharedPreference (for demo purpose only)
    public void clearLastTweetIdRecord(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(mContext.getString(R.string.twitter_last_tweet_id));
        editor.apply();
    }
}
