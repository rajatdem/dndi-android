package edu.cmu.msitese.dndiandroid.demoapp2;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.msitese.dndiandroid.R;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFramework;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFrameworkListener;

public class MainActivity extends AppCompatActivity implements
        SearchOnYelpTaskListener, PostTwitterTaskListener, DNDIFrameworkListener {

    private static final String TAG = "YELP_DEMO";

    private static final String APPBAR_TITLE = "Restaurant";
    private static final float MIN_DISTANCE_DELTA = 100f; // 100 meters
    private static final int DNDI_DELAY = 500;
    private static final int UPDATE_DELAY = 5000;

    private DNDIFramework dndi;
    private String mLastCategory = null;
    private Location mLastLocation = null;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private boolean hasPulledInBatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demoapp2_activity_main);
        setTitle(APPBAR_TITLE);

        mListView = (ListView) findViewById(R.id.listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // initialize the dndi framework
        dndi = new DNDIFramework(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean res = super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_tweet:
                // generate a dialogue for twitter post input
                fetchInputFromEditText();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void fetchInputFromEditText() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Enter your message here");
        alert.setTitle("Post a Tweet");
        alert.setIcon(R.drawable.twitter_logo_blue);
        alert.setView(edittext);

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                String input = edittext.getText().toString();
                Log.i(TAG, String.format("Input: %s", input));
                new PostTwitterTask(MainActivity.this).execute(input);
            }
        });
        alert.show();
    }

    @Override
    public void onSearchTaskCompleted(List<RestaurantInfoCell> results) {
        final RestaurantInfoListAdapter adapter = new RestaurantInfoListAdapter(this, results);

        // Attach the adapter to a ListView
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSearchTaskFailed(String message) {
        Log.i(TAG, "yelp search failed");
    }

    @Override
    public void onPostTaskCompleted() {

    }

    @Override
    public void onPostTaskFailed(String message) {
        Log.e(TAG, "failed to post a tweet: " + message);
    }

    @Override
    public void onInitializationCompleted() {
        new Timer().schedule(new DelayConfigCredential(), DNDI_DELAY);
    }

    @Override
    public void onInitializationTimeout() {

    }

    @Override
    public void onKeywordMatch(List<String> categories) {

        // always retrieve the first one as query
        String category = categories.get(0);

        Log.i(TAG, String.format("category update: %s", category));
        if(mLastLocation != null){
            if(!category.equals(mLastCategory)){
                // TODO: update the list view
                YelpSearchParameter params = new YelpSearchParameter();
                params.query = category;
                params.location = mLastLocation;
                new SearchOnYelpTask(this).execute(params);
            }
        }

        mLastCategory = category;
        if(!hasPulledInBatch){
            hasPulledInBatch = true;
            dndi.configTwitterEventMode();
        }
    }

    @Override
    public void onLastLocationUpdate(Location location) {

        Log.i(TAG, "location update");
        YelpSearchParameter params = new YelpSearchParameter();

        if(mLastLocation == null){
            mLastLocation = location;
            params.location = mLastLocation;

            if(mLastCategory != null){
                params.query = mLastCategory;
                new SearchOnYelpTask(this).execute(params);
            }
            else{
                // wait for five seconds before update
                new Timer().schedule(new DelayUpdateTask(params), UPDATE_DELAY);
            }
        }
        else{
            float distance = mLastLocation.distanceTo(location);
            Log.i(TAG, String.format("Distance: %f", distance));

            // update the last location, prevent update frequently
            if(distance > MIN_DISTANCE_DELTA){
                mLastLocation = location;
                if(mLastCategory != null){
                    params.query = mLastCategory;
                }
                params.location = location;
                new SearchOnYelpTask(this).execute(params);
            }
        }
    }

    class DelayConfigCredential extends TimerTask {

        @Override
        public void run() {
            TwitterCredentialDao dao = new TwitterCredentialDao(MainActivity.this);
            TwitterCredential credential = dao.getTwitterCredential();
            dndi.configTwitterCredential(
                    credential.accessToken,
                    credential.accessSecret,
                    credential.screenName);
            new Timer().schedule(new DelayPullTweet(), DNDI_DELAY);
        }
    }

    class DelayPullTweet extends TimerTask {

        @Override
        public void run() {
            dndi.pullTweetInBatchAll();
        }
    }

    class DelayUpdateTask extends TimerTask {

        private YelpSearchParameter mParam;

        public DelayUpdateTask(YelpSearchParameter param){
            mParam = param;
        }

        @Override
        public void run() {
            if(mLastCategory.isEmpty()){
                new SearchOnYelpTask(MainActivity.this).execute(mParam);
            }
        }
    }
}
