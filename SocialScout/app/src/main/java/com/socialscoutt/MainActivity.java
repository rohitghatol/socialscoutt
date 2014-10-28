package com.socialscoutt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.socialscoutt.utils.SocialConstants;

import java.io.InputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by rohitghatol on 10/4/14.
 */
public class MainActivity extends Activity {

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 100;
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static SharedPreferences mSharedPreferences;

    private Button twitterLoginInButton, facebookLoginButton, linkedInLoginButton;
    private ImageView twitterLogedInImg, facebookLogedInImg, linkedInLogedInImg;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
		/* initializing twitter parameters from string.xml */
        initTwitterConfigs();

		/* Enabling strict mode */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		/* Setting activity layout file */
        setContentView(com.socialscoutt.R.layout.activity_main);

        twitterLoginInButton = (Button)findViewById(R.id.btn_twitter_login);
        facebookLoginButton = (Button)findViewById(R.id.btn_facebook_login);
        linkedInLoginButton = (Button)findViewById(R.id.btn_linkedin_login);

        twitterLogedInImg = (ImageView)findViewById(R.id.twitter_loggedIn);
        facebookLogedInImg = (ImageView)findViewById(R.id.facebook_loggedIn);
        linkedInLogedInImg = (ImageView)findViewById(R.id.linkedin_loggedIn);

		/* Check if required twitter keys are set */
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

		/* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(SocialConstants.PREF_NAME, 0);
        boolean isTwitterLoggedIn = mSharedPreferences.getBoolean(SocialConstants.PREF_KEY_TWITTER_LOGIN, false);
        boolean isFacebookLoggedIn = mSharedPreferences.getBoolean(SocialConstants.PREF_KEY_FACEBOOK_LOGIN, false);
        boolean isLinkedLoggedIn = mSharedPreferences.getBoolean(SocialConstants.PREF_KEY_LINKEDIN_LOGIN, false);

        if(isTwitterLoggedIn){
            twitterLoginInButton.setVisibility(View.GONE);
            twitterLogedInImg.setVisibility(View.VISIBLE);
        }else{

            twitterLoginInButton.setVisibility(View.VISIBLE);
            twitterLogedInImg.setVisibility(View.GONE);
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(callbackUrl)) {
                String verifier = uri.getQueryParameter(oAuthVerifier);
                try {

					/* Getting oAuth authentication token */
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					/* Getting user id form access token */
                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    final String username = user.getName();

					/* save updated token */
                    saveTwitterInfo(accessToken);

                    final Intent intent = new Intent(this, ShareActivity.class);
                    startActivity(intent);

                } catch (Exception e) {
                    Log.e("Failed to login Twitter!!", e.getMessage());
                }
            }
        }

        if(isFacebookLoggedIn){
            facebookLoginButton.setVisibility(View.GONE);
            facebookLogedInImg.setVisibility(View.VISIBLE);
        }else {
            facebookLoginButton.setVisibility(View.VISIBLE);
            facebookLogedInImg.setVisibility(View.GONE);
        }

        if(isLinkedLoggedIn){
            linkedInLoginButton.setVisibility(View.GONE);
            linkedInLogedInImg.setVisibility(View.VISIBLE);
        }else {
            linkedInLoginButton.setVisibility(View.VISIBLE);
            linkedInLogedInImg.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = twitter.showUser(userID);

            String username = user.getName();

			/* Storing oAuth tokens to shared preferences */
            Editor e = mSharedPreferences.edit();
            e.putString(SocialConstants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(SocialConstants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(SocialConstants.PREF_KEY_TWITTER_LOGIN, true);
            e.putString(SocialConstants.PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(com.socialscoutt.R.string.twitter_consumer_key);
        consumerSecret = getString(com.socialscoutt.R.string.twitter_consumer_secret);
        callbackUrl = getString(com.socialscoutt.R.string.twitter_callback);
        oAuthVerifier = getString(com.socialscoutt.R.string.twitter_oauth_verifier);
    }


    public void loginToTwitter(View v) {
        boolean isLoggedIn = mSharedPreferences.getBoolean(SocialConstants.PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());

                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            final Intent intent = new Intent(this, ShareActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                final Intent intent = new Intent(this, ShareActivity.class);
                startActivity(intent);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loginToFacebook(View v) {
        Toast.makeText(this, getText(R.string.coming_soon),Toast.LENGTH_SHORT).show();
    }

    public void loginToLinkedIn(View v) {
        Toast.makeText(this, getText(R.string.coming_soon),Toast.LENGTH_SHORT).show();
    }
}
