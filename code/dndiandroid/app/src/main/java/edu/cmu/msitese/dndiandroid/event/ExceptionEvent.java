package edu.cmu.msitese.dndiandroid.event;

import com.bezirk.middleware.messages.Event;

/**
 * Created by Yu-Lun Tsai on 05/07/2017.
 */

public class ExceptionEvent extends Event {

    String mSource;
    Exception mException;

    public ExceptionEvent(String source, Exception exception){
        mSource = source;
        mException = exception;
    }
}
