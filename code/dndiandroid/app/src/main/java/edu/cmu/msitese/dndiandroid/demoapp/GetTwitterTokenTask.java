package edu.cmu.msitese.dndiandroid.demoapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import edu.cmu.msitese.dndiandroid.BuildConfig;
import edu.cmu.msitese.dndiandroid.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by Yu-Lun Tsai on 07/06/2017.
 */

public class GetTwitterTokenTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "APP";

    // GUI objects
    private Context mContext;
    private String mOAuthURL, mVerifier;
    private Dialog dialog;
    private WebView mWebView;
    private ProgressDialog mProgressBar;

    // Twitter variables
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private AccessToken mAccessToken;

    private static final String TWITTER_API_KEY = BuildConfig.TWITTER_API_KEY;
    private static final String TWITTER_API_SECRET = BuildConfig.TWITTER_API_SECRET;

    public GetTwitterTokenTask(Context context) {
        this.mContext = context;
        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(TWITTER_API_KEY, TWITTER_API_SECRET);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //showing a progress dialog
        mProgressBar = new ProgressDialog(mContext);
        mProgressBar.setMessage("Connecting...");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.setCancelable(false);
        mProgressBar.show();
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            mRequestToken = mTwitter.getOAuthRequestToken();
            mOAuthURL = mRequestToken.getAuthorizationURL();
        } catch (TwitterException e) {
            Log.e(TAG, e.toString());
        }
        return mOAuthURL;
    }

    @Override
    protected void onPostExecute(String oauthUrl) {

        if (oauthUrl != null) {

            dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setContentView(R.layout.dialog_oauth);
            mWebView = (WebView) dialog.findViewById(R.id.webView);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl(oauthUrl);

            mWebView.setWebViewClient(new WebViewClient() {

                boolean authComplete = false;

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    if (url.contains("oauth_verifier") && !authComplete) {
                        authComplete = true;
                        Uri uri = Uri.parse(url);
                        mVerifier = uri.getQueryParameter("oauth_verifier");
                        dialog.dismiss();

                        // evoke access token asynctask
                        new AccessTokenGetTask().execute();

                    } else if (url.contains("denied")) {
                        dialog.dismiss();
                        Log.e(TAG, "Permission is denied");
                        Toast.makeText(mContext, "Permission is denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            dialog.show();
            dialog.setCancelable(true);

            //dismiss progress dialog when task finished.
            mProgressBar.dismiss();

        } else {
            Toast.makeText(mContext, "Network Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private class AccessTokenGetTask extends AsyncTask<String, String, User> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar = new ProgressDialog(mContext);
            mProgressBar.setMessage("Fetching Data ...");
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressBar.setCancelable(false);
            mProgressBar.show();
        }

        @Override
        protected User doInBackground(String... args) {
            User user = null;
            try {
                mAccessToken = mTwitter.getOAuthAccessToken(mRequestToken, mVerifier);
                user = mTwitter.showUser(mAccessToken.getUserId());
                Log.i(TAG, "TOKEN: " + mAccessToken.getToken());
                Log.i(TAG, "SECRET: " + mAccessToken.getTokenSecret());
                Log.i(TAG, "ID: " + user.getScreenName());

            } catch (TwitterException e) {
                Log.e(TAG, e.toString());
            }
            return user;
        }

        @Override
        protected void onPostExecute(User response) {
            ((MainActivity) mContext).configTwitterCredential(
                    mAccessToken.getToken(),
                    mAccessToken.getTokenSecret(),
                    mAccessToken.getScreenName());
            mProgressBar.dismiss();
        }
    }
}
