package com.example.msitese.dndiandroid;

import com.bezirk.middleware.messages.Event;

import org.json.JSONObject;

/**
 * Created by larry on 30/03/2017.
 */

public class RawDataEvent extends Event {

    public enum GatherMode {
        BATCH,
        STREAMING,
    }

    public GatherMode type;
    public String content;
    public boolean hasText;
    public boolean hasLocation;

    public RawDataEvent(GatherMode type, JSONObject jsonObject){
        this.type = type;
        this.content = jsonObject.toString();
        this.hasText = false;
        this.hasLocation = false;
    }

}
