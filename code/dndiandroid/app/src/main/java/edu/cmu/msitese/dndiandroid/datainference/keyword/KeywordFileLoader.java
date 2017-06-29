package edu.cmu.msitese.dndiandroid.datainference.keyword;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.msitese.dndiandroid.R;

/**
 * Created by Yu-Lun Tsai on 19/06/2017.
 */

class KeywordFileLoader {

    private static final String TAG = "ZIRK";

    private Context context;

    KeywordFileLoader(Context context){
        this.context = context;
    }

    Map<String,String> loadKeywords(){

        // get data from text resource file containing JSON data.
        InputStream inputStream = context.getResources().openRawResource(R.raw.keywords);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // manually check whether reach the end of input byte stream
        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        // parse the JSON data and store it into a hash map
        Map<String,String> map = new HashMap<>();
        JSONArray categories;

        try {
            categories = new JSONArray(byteArrayOutputStream.toString());
            for(int i = 0; i < categories.length(); i++){

                JSONObject object = categories.getJSONObject(i);
                String category = object.getString("category");
                JSONArray keywords = object.getJSONArray("keywords");
                for(int j = 0; j < keywords.length(); j++){
                    String keyword = keywords.getString(j);
                    map.put(keyword, category);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return map;
    }
}
