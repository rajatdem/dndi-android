package edu.cmu.msitese.dndiandroid.datagathering.twitter;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cmu.msitese.dndiandroid.R;

/**
 * Created by Yu-Lun Tsai on 08/06/2017.
 */

public class TwitterDAO {

    private static final String TAG = "ZIRK";

    private Context mContext;

    public TwitterDAO(Context context){
        mContext = context;
    }

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

    public TwitterCredential getTwitterCredential(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String token = sharedPref.getString(mContext.getString(R.string.twitter_access_token), "");
        String secret = sharedPref.getString(mContext.getString(R.string.twitter_access_secret), "");
        String id = sharedPref.getString(mContext.getString(R.string.twitter_screen_name), "");
        return new TwitterCredential(token, secret, id);
    }

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

    public void saveLastTweetID(long id){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.twitter_last_tweet_id), Long.toString(id));
        editor.apply();
    }

    public long loadLastTweetID(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String res = sharedPref.getString(mContext.getString(R.string.twitter_last_tweet_id), "");

        if(res.equals(""))
            return -1;
        return Long.parseLong(res);
    }
}
