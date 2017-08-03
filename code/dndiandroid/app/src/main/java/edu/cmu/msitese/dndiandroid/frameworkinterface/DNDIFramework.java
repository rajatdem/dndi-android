package edu.cmu.msitese.dndiandroid.frameworkinterface;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONObject;

import edu.cmu.msitese.dndiandroid.R;
import edu.cmu.msitese.dndiandroid.Utils;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;

import java.util.ArrayList;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class DNDIFramework {

    // static tag with string type used for filtering console output
    private static final String TAG = "ZIRK";

    // members used for interact with the config service
    public static final String KEYWORD_MATCH = "cmu.edu.msitese.dndiandroid.DNDIFramework.MATCH_EVENT";
    public static final String RAW_LOCATION = "cmu.edu.msitese.dndiandroid.DNDIFramework.RAW_LOCATION";
    public static final String ERROR = "cmu.edu.msitese.dndiandroid.DNDIFramework.ERROR";

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Context mContext;
    private ZirkManagerService mZirkManagerService;
    private boolean isBound = false;

    // the service connection callback, it will get the service instance once the binding is completed
    private ServiceConnection mServerConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mZirkManagerService = ((ZirkManagerService.ZirkManagerServiceBinder) binder).getService();
            isBound = true;
            if(mContext instanceof DNDIFrameworkListener){
                ((DNDIFrameworkListener) mContext).onInitializationCompleted();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // the broadcast receiver callback that parse the intent and call corresponding callback
    // functions
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String result = intent.getStringExtra(mContext.getString(R.string.intent_result));

            switch (result){
                case KEYWORD_MATCH:
                    ArrayList<String> keywords = intent.getStringArrayListExtra(
                            mContext.getString(R.string.intent_result_keyword));
                    if(mContext instanceof DNDIFrameworkListener){
                        ((DNDIFrameworkListener) mContext).onKeywordMatch(keywords);
                    }
                    break;
                case RAW_LOCATION:
                    Location location = intent.getParcelableExtra(
                            mContext.getString(R.string.intent_result_raw_location));
                    if(mContext instanceof DNDIFrameworkListener){
                        ((DNDIFrameworkListener) mContext).onLastLocationUpdate(location);
                    }
                    break;
                case ERROR:
                    break;
                default:
                    break;
            }
        }
    };

    // DNDIFramework constructor, the user should at least specify the context for it
    public DNDIFramework(Context context){
        this.mContext = context;
        bindToConfigService();
    }

    // bind to the broadcast sent by config service
    public void resume(){
        Log.i(TAG, "onResume is called");
        IntentFilter filter = new IntentFilter(ZirkManagerService.ACTION);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, filter);
   }

    // unbind the broadcast sent by the config service
    public void pause(){
        Log.i(TAG, "onPaused is called");
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }

    // unbind the config service
    public void stop(){
        if(isBound){
            mContext.unbindService(mServerConn);
            isBound = false;
        }
    }

    // return true if the DNDI framework is ready to be interacted with
    public boolean ready(){
        return isBound;
    }

    public void pullTweetInBatchAll(){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PULL);

            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void pullTweetInBatchByNum(int num){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PULL, Integer.toString(num));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterEventMode(){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_EVENT);
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterPeriodicMode(int period){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void periodicGPS(int period){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void eventGPS(int shortestDist){
        if (isBound) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT, Integer.toString(shortestDist));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    // send twitter credential through the bezirk middleware
    public void configTwitterCredential(String token, String secret, String id){
        if (isBound) {
            JSONObject jsonObject = Utils.packCredentialToJSON(token, secret, id);
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_CONFIG_API_KEY, jsonObject.toString());
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    // bind to the config service
    private void bindToConfigService(){

        if(isServiceRunning(ZirkManagerService.class)){
            mContext.bindService(new Intent(mContext, ZirkManagerService.class), mServerConn, Context.BIND_NOT_FOREGROUND);
        }
        else{
            mContext.startService(new Intent(mContext, ZirkManagerService.class));
            mContext.bindService(new Intent(mContext, ZirkManagerService.class), mServerConn, Context.BIND_NOT_FOREGROUND);
        }
    }

    // check whether a service is running on the device
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopGPSService() {
        mZirkManagerService.stopGPSService();
    }
}
