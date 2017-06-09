package com.example.msitese.dndiandroid;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class DNDIFramework {

    private static final String TAG = "ZIRK";

    private Context mContext;
    private ConfigService mConfigServiceBinder;

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mConfigServiceBinder = ((ConfigService.ConfigServiceBinder) binder).getService();
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    public DNDIFramework(Context context){

        this.mContext = context;

        if(isServiceRunning(ConfigService.class)){
            // bind service here
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
        }
        else{
            mContext.startService(new Intent(mContext, ConfigService.class));
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
        }
    }

    public boolean addKeyword(String keyword){
        return true;
    }

    public boolean deleteKeyword(String keyword){
        return true;
    }

    public void pullDataInBatch(){
        if (mConfigServiceBinder != null) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PULL);
            mConfigServiceBinder.sendBezirkEvent(evt);
        }
    }

    public void configEventMode(){
        if (mConfigServiceBinder != null) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT);
            mConfigServiceBinder.sendBezirkEvent(evt);
        }
    }

    public void configPeriodicMode(){
        if (mConfigServiceBinder != null) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC);
            mConfigServiceBinder.sendBezirkEvent(evt);
        }
    }

    public void setTwitterAccessToken(){
        if (mConfigServiceBinder != null) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_CONFIG_API_KEY);
            mConfigServiceBinder.sendBezirkEvent(evt);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
