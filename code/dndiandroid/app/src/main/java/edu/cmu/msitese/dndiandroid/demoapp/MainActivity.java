package edu.cmu.msitese.dndiandroid.demoapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

//import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.R;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterDao;
import edu.cmu.msitese.dndiandroid.datainference.keyword.KeywordCountDao;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFramework;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFrameworkListener;


public class MainActivity extends AppCompatActivity implements DNDIFrameworkListener {

    private static final String TAG = "APP";
    private DNDIFramework dndi;

    private EditText mEditTestInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // config layout and UI components
        setContentView(edu.cmu.msitese.dndiandroid.R.layout.activity_main);
        configUIComponents();

        // initialize the DNDI framework
        dndi = new DNDIFramework(this);
    }

    @Override
<<<<<<< HEAD
    protected void onResume(){
        super.onResume();
        dndi.resume();
    }

    @Override
    protected void onPause(){
        dndi.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        dndi.stop();
        super.onDestroy();
   }

    @Override
    public void onKeywordMatch(List<String> keywords) {

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < keywords.size(); i++){
            sb.append(" ");
            sb.append(keywords.get(i));
        }

        // generate a list of topics
        sendNotification(sb.toString());

        // display the matched topic in toast
        Toast.makeText(getBaseContext(), "Match with category:" + sb.toString(), Toast.LENGTH_LONG).show();
    }

    private void sendNotification(String category){

        // create notification builder and set notification properties
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("A Bezirk Notification, Click Me!");
        mBuilder.setContentText("Hi, Here is the latest coupon for (" + category + "). Click for more details!");
        mBuilder.setAutoCancel(true);

        // attach an action
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("match", category);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // issue a notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());

        Log.i(TAG, this.getClass().getName() + ":: Send Android notification...");
    }

    private void configUIComponents(){
        mEditTestInput = (EditText) findViewById(edu.cmu.msitese.dndiandroid.R.id.etInput);
    }

    public void onClickPull(View view){
        dndi.pullDataInBatchAll();
    }

    public void onClickPeriodic(View view){

        int num;
        // convert the input string to integer
        try{
            num = Integer.valueOf(mEditTestInput.getText().toString());
        }
        catch (NumberFormatException ex){
            // if the string is invalid, assign a default value
            num = 30000;
        }
        dndi.configPeriodicMode(num);
    }

    public void onClickEvent(View view){
        dndi.configEventMode();
    }

    // Twitter related application code
    public void onClickTwitterOAuth(View view) {
        new GetTwitterTokenTask(this).execute();
    }

    public void configTwitterCredential(String token, String secret, String screenName){
        dndi.configTwitterCredential(token, secret, screenName);
    }

    public void onClickClearPreference(View view){
        TwitterDao twitterDao = new TwitterDao(this);
        twitterDao.clearTwitterCredential();

        KeywordCountDao keywordCountDao = new KeywordCountDao(this);
        keywordCountDao.clearTable();
    }

    public void onClickDebugInferredResult(View view){
        KeywordCountDao dao = new KeywordCountDao(this);
        dao.printContentToConsole();
    }

    public void onClickGPS(View view){
        //TODO: GET the GPS Coordinates.
//        startService(new Intent(getBaseContext(), LocationDataService.class));
    }

    // Method to stop the service
    public void stopServiceGPS(View view) {
//        stopService(new Intent(getBaseContext(), LocationDataService.class));
    }
}
