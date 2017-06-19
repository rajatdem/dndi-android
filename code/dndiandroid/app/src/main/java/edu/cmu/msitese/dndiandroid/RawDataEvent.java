package edu.cmu.msitese.dndiandroid;

import com.bezirk.middleware.messages.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yu-Lun Tsai on 10/06/2017.
 */

public class RawDataEvent extends Event {

    public enum GatherMode {
        BATCH,
        PERIODIC,
        STREAMING,
    }

    public RawDataEvent.GatherMode mode;
    public boolean hasText;
    public boolean hasLocation;
    private List<RawData> array;

    public RawDataEvent(GatherMode mode){
        this.mode = mode;
        array = new ArrayList<>();
    }

    public void appendRawData(RawData rawData){
        array.add(rawData);
    }

    public String toString(){

        StringBuilder sb = new StringBuilder();
        for(RawData data: array){
            sb.append("\n=======================");
            sb.append("\nText: " + data.text);
            sb.append("\nDate: " + data.date);
            sb.append("\nLocation: " + data.location);
        }
        return sb.toString();
    }
}
