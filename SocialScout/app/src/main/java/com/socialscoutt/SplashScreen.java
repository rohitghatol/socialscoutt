package com.socialscoutt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.socialscoutt.utils.SocialConstants;


public class SplashScreen extends Activity {

    private static SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        /* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(SocialConstants.PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(SocialConstants.PREF_KEY_TWITTER_LOGIN, false);

        if (isLoggedIn) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            setContentView(com.socialscoutt.R.layout.activity_splash_screen);
        }
    }

    public void onProceed(View view) {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.socialscoutt.R.menu.splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == com.socialscoutt.R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
