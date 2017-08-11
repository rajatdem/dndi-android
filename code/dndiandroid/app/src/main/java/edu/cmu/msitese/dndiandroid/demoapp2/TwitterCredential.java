package edu.cmu.msitese.dndiandroid.demoapp2;

import java.io.Serializable;

/**
 * Created by Yu-Lun Tsai on 01/08/2017.
 */

public class TwitterCredential implements Serializable {

    String accessToken;
    String accessSecret;
    String screenName;

    public TwitterCredential(String token, String secret, String name){
        accessToken = token;
        accessSecret = secret;
        screenName = name;
    }
}
