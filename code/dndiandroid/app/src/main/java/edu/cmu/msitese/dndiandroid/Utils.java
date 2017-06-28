package edu.cmu.msitese.dndiandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterCredential;
import edu.cmu.msitese.dndiandroid.event.RawData;
import twitter4j.Status;

/**
 * Created by Yu-Lun Tsai on 04/06/2017.
 */

public class Utils {

    private static final String TAG = "UTILS";

    public static JSONObject packCredentialToJSON(String token, String secret, String name){

        JSONObject result = new JSONObject();
        try {
            result.put("token", token);
            result.put("secret", secret);
            result.put("screenName", name);
        } catch (JSONException e) {
            Log.w(TAG, e.toString());
        }
        return result;
    }

    public static TwitterCredential getTwitterCredentialFromJSONRaw(String raw){

        JSONObject object;
        TwitterCredential twitterCredential = null;
        try {
            object = new JSONObject(raw);
            if(!object.has("token")||!object.has("secret")||!object.has("screenName")){
                return twitterCredential;
            }
            twitterCredential = new TwitterCredential(
                    object.getString("token"),
                    object.getString("secret"),
                    object.getString("screenName"));
        } catch (JSONException e) {
            Log.w(TAG, e.toString());
        }
        return twitterCredential;
    }

    public static RawData packTweetToRawDataFormat(Status status){
        String place = (status.getPlace() != null) ? status.getPlace().getFullName() : null;
        RawData rawData = new RawData();
        rawData.setLocation(place);
        rawData.setText(status.getText());
        rawData.setDate(Long.toString(status.getCreatedAt().getTime()));
        return rawData;
    }
}
