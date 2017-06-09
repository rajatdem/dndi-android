package com.example.msitese.dndiandroid;

import com.bezirk.middleware.messages.Event;

/**
 * Created by larry on 30/03/2017.
 */

class CommandEvent extends Event {

    enum CmdType {
        CMD_DUMMY,
        CMD_CONFIG_API_KEY,
        CMD_PULL,
        CMD_PERIODIC,
        CMD_EVENT,
    }

    public CmdType type = CmdType.CMD_DUMMY;
    public String extra = "";

    public CommandEvent(CmdType type){
        this.type = type;
    }

    public CommandEvent(CmdType type, String extra){
        this.type = type;
        this.extra = extra;
    }
}
