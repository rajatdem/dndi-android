package edu.cmu.msitese.dndiandroid;

import java.io.Serializable;

/**
 * Created by Yu-Lun Tsai on 10/06/2017.
 */

public class RawData implements Serializable {
    private String date;
    private String text;
    private String location;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {

        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
