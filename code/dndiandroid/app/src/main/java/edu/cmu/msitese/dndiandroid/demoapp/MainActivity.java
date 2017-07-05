package edu.cmu.msitese.dndiandroid.demoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

//import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterDao;
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
        int max = Math.min(2, keywords.size());
        for(int i = 0; i < max; i++){
            sb.append(" ");
            sb.append(keywords.get(i));
        }
        Toast.makeText(getBaseContext(), "Match keywords:" + sb.toString(), Toast.LENGTH_LONG).show();
    }

    private void configUIComponents(){
        mEditTestInput = (EditText) findViewById(edu.cmu.msitese.dndiandroid.R.id.etInput);
    }

    public void onClickPull(View view){
        dndi.pullDataInBatch();
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
        TwitterDao dao = new TwitterDao(this);
        dao.clearTwitterCredential();
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
