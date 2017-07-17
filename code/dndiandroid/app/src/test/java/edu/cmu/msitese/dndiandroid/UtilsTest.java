package edu.cmu.msitese.dndiandroid;

import org.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import edu.cmu.msitese.dndiandroid.datagathering.twitter.TwitterCredential;
import edu.cmu.msitese.dndiandroid.event.RawData;
import twitter4j.Status;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 * Created by Yu-Lun Tsai on 05/06/2017.
 */

public class UtilsTest {


    @Test
    public void testGivenTwitterStatusThenOutputRaw(){

        //  create mock
        Status status = mock(Status.class);
        String text = "Dummy tweet.";

        // define return value for methods
        when(status.getGeoLocation()).thenReturn(null);
        when(status.getCreatedAt()).thenReturn(new Date());
        when(status.getText()).thenReturn(text);
        when(status.getPlace()).thenReturn(null);

        // unit test target
        RawData rawData = Utils.packTweetToRawDataFormat(status);

        // the text should be set successfully
        assertEquals(text, rawData.getText());
    }

    @Test
    public void testTwitterCredentialPackingAnUnpacking() throws Exception {

        String token = "sfsgksjdflgjlsd";
        String secret = "sddsfgsdgksdjglsdkgjl";
        String name = "sggfdsdgfsdg";

        // normal case
        JSONObject object = Utils.packCredentialToJSON(token, secret, name);
        TwitterCredential credential = Utils.getTwitterCredentialFromJSONRaw(object.toString());

        assertEquals(token, credential.token);
        assertEquals(secret, credential.secret);
        assertEquals(name, credential.screenName);

        // edge cases
        object = new JSONObject();
        credential = Utils.getTwitterCredentialFromJSONRaw(object.toString());
        assertNull(credential);
    }
}
