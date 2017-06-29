package edu.cmu.msitese.dndiandroid.datagathering.twitter;

/**
 * Created by Yu-Lun Tsai on 08/06/2017.
 */

public class TwitterCredential {

    public String screenName;
    public String token;
    public String secret;

    public TwitterCredential(String token, String secret, String screenName){
        this.token = token;
        this.secret = secret;
        this.screenName = screenName;
    }
}
