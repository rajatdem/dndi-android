package edu.cmu.msitese.dndiandroid;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterCredential;
import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Yu-Lun Tsai on 13/06/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class TwitterDaoTest {

    private Context mContext;
    private SharedPreferences mSharedPrefs;

    @Before
    public void init(){

        mContext = mock(Context.class);
        mSharedPrefs = mock(SharedPreferences.class);
        when(mContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mSharedPrefs);
        when(mContext.getString(R.string.preference_file_key)).thenReturn("dummyString");
    }

    @Test
    public void testSaveTwitterCredential() throws Exception {

        SharedPreferences.Editor mEditor = mock(SharedPreferences.Editor.class);
        SharedPreferences.Editor spyEditor = spy(mEditor);
        when(mSharedPrefs.edit()).thenReturn(spyEditor);

        when(mContext.getString(R.string.twitter_access_token)).thenReturn("were");
        when(mContext.getString(R.string.twitter_access_secret)).thenReturn("werewr");
        when(mContext.getString(R.string.twitter_screen_name)).thenReturn("werwrwr");

        TwitterDao twitterDao = new TwitterDao(mContext);
        twitterDao.saveTwitterCredential("sdfsdfsdf", "sfsdfsdf", "sfdffd");

        verify(spyEditor, times(1)).commit();

        twitterDao.clearTwitterCredential();
        verify(spyEditor, times(1)).apply();
    }

    @Test
    public void testGetTwitterCredential() throws Exception {

        String token = "token";
        String secret = "secret";
        String name = "name";

        String twitterToken = "xdfsgfdgsdgfsd";
        String twitterSecret = "sdfsderdgssd";
        String twitterScreenName = "ssdfgdsdgdsgs";

        when(mContext.getString(R.string.twitter_access_token)).thenReturn(token);
        when(mContext.getString(R.string.twitter_access_secret)).thenReturn(secret);
        when(mContext.getString(R.string.twitter_screen_name)).thenReturn(name);

        when(mSharedPrefs.getString(token, "")).thenReturn(twitterToken);
        when(mSharedPrefs.getString(secret, "")).thenReturn(twitterSecret);
        when(mSharedPrefs.getString(name, "")).thenReturn(twitterScreenName);

        TwitterDao twitterDao = new TwitterDao(mContext);
        TwitterCredential credential = twitterDao.getTwitterCredential();

        assertEquals(twitterToken, credential.token);
        assertEquals(twitterSecret, credential.secret);
        assertEquals(twitterScreenName, credential.screenName);
    }

    @Test
    public void testSaveLastTweetId() throws Exception {

        SharedPreferences.Editor mEditor = mock(SharedPreferences.Editor.class);
        SharedPreferences.Editor spyEditor = spy(mEditor);
        when(mSharedPrefs.edit()).thenReturn(spyEditor);

        TwitterDao twitterDao = new TwitterDao(mContext);
        twitterDao.saveLastTweetId(100);
        verify(spyEditor, times(1)).apply();

        twitterDao.clearTwitterCredential();
        verify(spyEditor, times(2)).apply();
    }

    @Test
    public void testLoadLastTweetId() throws Exception {

        String key = "id";
        String twitterIdStr = "100";

        when(mContext.getString(R.string.twitter_last_tweet_id)).thenReturn(key);
        when(mSharedPrefs.getString(key, "")).thenReturn(twitterIdStr);

        TwitterDao twitterDao = new TwitterDao(mContext);
        long id = twitterDao.loadLastTweetId();

        assertEquals(id, Long.parseLong(twitterIdStr));
    }
}
