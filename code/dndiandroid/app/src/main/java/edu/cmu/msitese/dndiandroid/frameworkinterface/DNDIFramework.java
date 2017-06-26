package edu.cmu.msitese.dndiandroid.frameworkinterface;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;

//import org.json.JSONObject;
//import edu.cmu.msitese.dndiandroid.Utils;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;

import java.util.ArrayList;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class DNDIFramework {

    private static final String TAG = "ZIRK";

    public static final String KEYWORD_MATCH = "cmu.edu.msitese.dndiandroid.DNDIFramework.MATCH_EVENT";
    public static final String ERROR = "cmu.edu.msitese.dndiandroid.DNDIFramework.ERROR";

    private Context mContext;
    private ConfigService mConfigService;
    private boolean isBound = false;

    private ServiceConnection mServerConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mConfigService = ((ConfigService.ConfigServiceBinder) binder).getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            switch (result){
                case KEYWORD_MATCH:
                    ArrayList<String> keywords = intent.getStringArrayListExtra("keywords");
                    ((DNDIFrameworkListener) mContext).onKeywordMatch(keywords);
                    break;
                case ERROR:
                    break;
            }
        }
    };

    public DNDIFramework(Context context){
        this.mContext = context;
        bindToConfigService();
    }

    public void resume(){
        IntentFilter filter = new IntentFilter(ConfigService.ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, filter);
    }

    public void pause(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    public void stop(){
        if(isBound){
            mContext.unbindService(mServerConn);
        }
    }

    public boolean ready(){
        return isBound;
    }

    public void pullDataInBatch(){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PULL);
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configEventMode(){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT);
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configPeriodicMode(int period){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mConfigService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterCredential(String token, String secret, String id){
        if (isBound) {
//            JSONObject jsonObject = Utils.packCredentialToJSON(token, secret, id);
//            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_CONFIG_API_KEY, jsonObject.toString());
//            mConfigService.sendBezirkEvent(evt);
        }
    }

    private void bindToConfigService(){

        if(isServiceRunning(ConfigService.class)){
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
        }
        else{
            mContext.startService(new Intent(mContext, ConfigService.class));
            mContext.bindService(new Intent(mContext, ConfigService.class), mServerConn, Context.BIND_AUTO_CREATE);
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
