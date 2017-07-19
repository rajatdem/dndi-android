package edu.cmu.msitese.dndiandroid.event;

import com.bezirk.middleware.messages.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yu-Lun Tsai on 19/06/2017.
 */

public class ResultEvent extends Event {

    private Map<String, Integer> occurrence = new HashMap<>();

    public void increaseMatchOccurrence(String key){
        int count = 0;
        if(occurrence.containsKey(key)){
            count = occurrence.get(key);
        }
        occurrence.put(key, count + 1);
    }

    public boolean hasMatch(){
        return !occurrence.isEmpty();
    }

    public ArrayList<String> getMatchList(){
        return new ArrayList<>(occurrence.keySet());
    }

    public int getMatchCount(String key){
        int count = 0;
        if(occurrence.containsKey(key)){
            count = occurrence.get(key);
        }
        return count;
    }

}
