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
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import edu.cmu.msitese.dndiandroid.R;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFramework;
import edu.cmu.msitese.dndiandroid.frameworkinterface.DNDIFrameworkListener;

public class MainActivity extends AppCompatActivity implements
        SearchOnYelpTaskListener, PostTwitterTaskListener, DNDIFrameworkListener {

    private static final String TAG = "YELP_DEMO";

    private static final String APPBAR_TITLE = "Restaurant";
    private DNDIFramework dndi;
    private Location mLastLocation = null;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demoapp2_activity_main);
        setTitle(APPBAR_TITLE);
        loadUIComponents();

        new SearchOnYelpTask(this).execute();

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

    private void loadUIComponents(){
        mListView = (ListView) findViewById(R.id.listView);
    }

    private void fetchInputFromEditText() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Enter your message");
        alert.setTitle("Post A Tweet");
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

    private void pullUserHistoricalTweets(){
        dndi.pullTweetInBatchAll();
    }

    @Override
    public void onSearchTaskCompleted(List<RestaurantInfoCell> results) {
        final RestaurantInfoListAdapter adapter = new RestaurantInfoListAdapter(this, results);
        // Attach the adapter to a ListView
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchTaskFailed(String message) {
        Log.i(TAG, "yelp search failed");
    }

    @Override
    public void onPostTaskCompleted() {
        Log.i(TAG, "post a tweet successfully");
    }

    @Override
    public void onPostTaskFailed(String message) {
        Log.i(TAG, "failed to post a tweet: " + message);
    }

    @Override
    public void onInitializationCompleted() {
        Log.i(TAG, "the dndi is initialized successfully");
    }

    @Override
    public void onKeywordMatch(List<String> keywords) {

    }

    @Override
    public void onLastLocationUpdate(Location location) {

        if(mLastLocation == null){
            mLastLocation = location;
        }
        else{

        }
    }
}
