package edu.cmu.msitese.dndiandroid.event;

import com.bezirk.middleware.messages.Event;

/**
 * Created by larry on 30/03/2017.
 */

public class CommandEvent extends Event {

    public enum CmdType {
        CMD_DUMMY,
        CMD_CONFIG_API_KEY,
        CMD_PULL,
        CMD_PERIODIC,
        CMD_EVENT,
    }

    public String target = "";
    public CmdType type = CmdType.CMD_DUMMY;
    public String extra = "";

    public CommandEvent(CmdType type){
        this.type = type;
    }

    public CommandEvent(CmdType type, String extra){
        this.type = type;
        this.extra = extra;
    }

    // include target in the constructor
    public CommandEvent(String target, CmdType type){
        this.type = type;
        this.target = target;
    }

    public CommandEvent(String target, CmdType type, String extra){
        this.type = type;
        this.target = target;
        this.extra = extra;
    }
}
