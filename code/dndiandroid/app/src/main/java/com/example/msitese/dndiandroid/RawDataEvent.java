package com.example.msitese.dndiandroid;

import com.bezirk.middleware.messages.Event;

import org.json.JSONObject;

/**
 * Created by larry on 30/03/2017.
 */

public class RawDataEvent extends Event {

    public enum GatherMode {
        MODE_BATCH,
        MODE_STREAMING,
    }

    private GatherMode type;
    private String content;

    public RawDataEvent(GatherMode type, JSONObject jsonObject){
        this.type = type;
        this.content = jsonObject.toString();
    }

    public GatherMode getType(){
        return type;
    }

    public String getContent(){
        return this.content;
    }
}
