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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class DNDIFramework {

    // static tag with string type used for filtering console output
    private static final String TAG = "ZIRK";

    // constants used to check whether bezirk is initialized
    private static final int CHECK_BEZIRK_DELAY = 100;
    private static final int CHECK_BEZIRK_PERIOD = 1000;
    private static final int MAXIMUM_CHECK_FAIL = 5;

    // members used for interact with the config service
    public static final String RESULT = "DNDIFramework.result";
    public static final String KEYWORD_MATCHED = "DNDIFramework.result.KEYWORD_MATCHED";
    public static final String RAW_LOCATION = "DNDIFramework.result.RAW_LOCATION";
    public static final String MANAGER_INITIALIZED = "DNDIFramework.result.MANAGER_INITIALIZED";
    public static final String ERROR = "DNDIFramework.result.ERROR";

    private Context mContext;
    private ZirkManagerService mZirkManagerService;
    private Timer mTimer;
    private int timerCount;
    private boolean isConnected = false;

    // the service connection callback, it will get the service instance once the binding is completed
    private ServiceConnection mServerConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mZirkManagerService = ((ZirkManagerService.ZirkManagerServiceBinder) binder).getService();
            timerCount = 0;
            mTimer = new Timer();
            mTimer.schedule(new CheckZirkManagerInitialization(), CHECK_BEZIRK_DELAY, CHECK_BEZIRK_PERIOD);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    // the broadcast receiver callback that parse the intent and call corresponding callback
    // functions
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String result = intent.getStringExtra(RESULT);
            switch (result){
                case KEYWORD_MATCHED:
                    ArrayList<String> keywords = intent.getStringArrayListExtra(KEYWORD_MATCHED);
                    if(mContext instanceof DNDIFrameworkListener){
                        ((DNDIFrameworkListener) mContext).onKeywordMatch(keywords);
                    }
                    break;
                case RAW_LOCATION:
                    Location location = intent.getParcelableExtra(RAW_LOCATION);
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

        // bind to the ZirkManagerService
        bindToConfigService();
    }

    class CheckZirkManagerInitialization extends TimerTask {

        @Override
        public void run() {
            if(mZirkManagerService.isBezirkInitialized()){
                isConnected = true;
                mTimer.cancel();
                mTimer.purge();
                if(mContext instanceof DNDIFrameworkListener){
                    ((DNDIFrameworkListener) mContext).onInitializationCompleted();
                }
            }
            else{
                timerCount++;
                if(timerCount > MAXIMUM_CHECK_FAIL){
                    // TODO: a callback to notify main activity that connection failed
                }
            }
        }
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
        if(isConnected){
            mContext.unbindService(mServerConn);
            isConnected = false;
        }
    }

    // return true if the DNDI framework is ready to be interacted with
    public boolean ready(){
        return isConnected;
    }

    public void pullTweetInBatchAll(){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PULL);

            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void pullTweetInBatchByNum(int num){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PULL, Integer.toString(num));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterEventMode(){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_EVENT);
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void configTwitterPeriodicMode(int period){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void periodicGPS(int period){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_PERIODIC, Integer.toString(period));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    public void eventGPS(int shortestDist){
        if (isConnected) {
            final CommandEvent evt = new CommandEvent(CommandEvent.CmdType.CMD_EVENT, Integer.toString(shortestDist));
            mZirkManagerService.sendBezirkEvent(evt);
        }
    }

    // send twitter credential through the bezirk middleware
    public void configTwitterCredential(String token, String secret, String id){
        if (isConnected) {
            JSONObject jsonObject = Utils.packCredentialToJSON(token, secret, id);
            final CommandEvent evt = new CommandEvent(
                    mContext.getString(R.string.target_twitter),
                    CommandEvent.CmdType.CMD_CONFIG_API_KEY,
                    jsonObject.toString()
            );
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
