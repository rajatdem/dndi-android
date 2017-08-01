package edu.cmu.msitese.dndiandroid.frameworkinterface;

import java.util.List;

/**
 * Created by Yu-Lun Tsai on 09/06/2017.
 */

public interface DNDIFrameworkListener {

    void onKeywordMatch(List<String> keywords);
    void onLastLocationUpdate(double latitude, double longitude);
}
