package edu.cmu.msitese.dndiandroid.frameworkinterface;

import android.location.Location;

import java.util.List;

/**
 * Created by Yu-Lun Tsai on 09/06/2017.
 */

public interface DNDIFrameworkListener {
    void onInitializationCompleted();
    void onKeywordMatch(List<String> keywords);
    void onLastLocationUpdate(Location location);
}
