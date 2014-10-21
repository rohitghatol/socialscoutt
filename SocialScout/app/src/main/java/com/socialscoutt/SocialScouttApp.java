package com.socialscoutt;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

/**
 * Created by saurabhgangarde on 15/10/14.
 */
public class SocialScouttApp extends Application {

    public SocialScouttApp() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "12rciFnh3UxUprA921LgvOIcdRxHwPfZKKM6P2GZ", "EGBBtRBfTX3AlsDN6kIWQK64VVoaWlRux5SDXqB9");

        // Specify an Activity to handle all pushes by default.
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }
}
