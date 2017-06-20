package edu.cmu.msitese.dndiandroid.framework;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import edu.cmu.msitese.dndiandroid.event.CommandEvent;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class DNDIFramework {

    private static final String TAG = "ZIRK";

    private Context mContext;
    private ConfigService mConfigService;

    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mConfigService = ((ConfigService.ConfigServiceBinder) binder).getService();
            Log.i(TAG, "DNDI is connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "DNDI is disconnected");
        }
    };

    public DNDIFramework(Context context){

        this.mContext = context;

        if(isServiceRunning(ConfigService.class)){
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
        }
        else{
            mContext.startService(new Intent(mContext, ConfigService.class));
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
        }
    }

    public boolean interfaceReady(){
        return (mConfigService != null);
    }

    public boolean addKeyword(String keyword){
        return true;
    }

    public boolean deleteKeyword(String keyword){
        return true;
    }

    public void pullDataInBatch(){
        if (interfaceReady()) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PULL);
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configEventMode(){
        if (interfaceReady()) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT);
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configPeriodicMode(int period){
        if (mConfigService != null) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterCredential(String token, String secret, String id){
        if (interfaceReady()) {
//            JSONObject jsonObject = Utils.packCredentialToJSON(token, secret, id);
//            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_CONFIG_API_KEY, jsonObject.toString());
//            mConfigService.sendBezirkEvent(evt);
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public DNDIFramework(Context context, ServiceConnection conn){
        this.mContext = context;
        if(isServiceRunning(ConfigService.class)){
            mContext.bindService(new Intent(mContext, ConfigService.class), conn, Context.BIND_AUTO_CREATE);
        }
        else{
            mContext.startService(new Intent(mContext, ConfigService.class));
            mContext.bindService(new Intent(mContext, ConfigService.class), conn, Context.BIND_AUTO_CREATE);
        }
    }
}
