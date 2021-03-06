package edu.cmu.msitese.dndiandroid.demoapp2;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cmu.msitese.dndiandroid.R;

/**
 * Created by Yu-Lun Tsai on 01/08/2017.
 */

public class TwitterCredentialDao {

    private Context mContext;

    public TwitterCredentialDao(Context context){
        mContext = context;
    }

    public boolean checkTwitterCredential(){

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.app_sp),
                Context.MODE_PRIVATE);
        String token = sharedPref.getString(mContext.getString(R.string.app_twitter_token), "");
        return !token.isEmpty();
    }

    public TwitterCredential getTwitterCredential(){

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.app_sp),
                Context.MODE_PRIVATE);
        String token = sharedPref.getString(mContext.getString(R.string.app_twitter_token), "");
        String secret = sharedPref.getString(mContext.getString(R.string.app_twitter_secret), "");
        String name = sharedPref.getString(mContext.getString(R.string.app_twitter_name), "");

        if(token.isEmpty()){
            return null;
        }
        else{
            return new TwitterCredential(token, secret, name);
        }
    }

    public void saveTwitterCredential(String token, String secret, String name){

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                mContext.getString(R.string.app_sp),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(mContext.getString(R.string.app_twitter_token), token);
        editor.putString(mContext.getString(R.string.app_twitter_secret), secret);
        editor.putString(mContext.getString(R.string.app_twitter_name), name);
        editor.commit();
    }
}
