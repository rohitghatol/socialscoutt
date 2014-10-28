package com.socialscoutt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.socialscoutt.utils.SocialConstants;
import com.socialscoutt.utils.Utils;

import java.io.InputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class ShareActivity extends Activity {

    private ProgressDialog pDialog;
    private EditText mShareEditText;
    private TextView userName;
    private static SharedPreferences mSharedPreferences;

    private String consumerKey = null;
    private String consumerSecret = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        /* initializing twitter parameters from string.xml */
        initTwitterConfigs();

        mSharedPreferences = getSharedPreferences(SocialConstants.PREF_NAME, 0);
        String username = mSharedPreferences.getString(SocialConstants.PREF_USER_NAME, "");

        mShareEditText = (EditText) findViewById(R.id.share_text);
        userName = (TextView)findViewById(R.id.user_name);
        userName.setText(getText(R.string.hello) + " " + username);

        String topicId = getIntent().getStringExtra(SocialConstants.TOPIC_ID);
        if(null != topicId && topicId.length() > 0){
            getArticleDetails(topicId);
        }
    }

    private void getArticleDetails(String topicId) {
        ParseQuery<ParseObject> pQuery = new ParseQuery<ParseObject>("Article");
        try {
            ParseObject parseObject = pQuery.get(topicId);
            String message = (String)parseObject.get("message");
            mShareEditText.setText(message);
            Utils.discardNotification(this,1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            final Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(com.socialscoutt.R.string.twitter_consumer_key);
        consumerSecret = getString(com.socialscoutt.R.string.twitter_consumer_secret);
    }

    public void shareClick(View view){
        final String status = mShareEditText.getText().toString();

        if (status.trim().length() > 0) {
            new updateTwitterStatus().execute(status);
        } else {
            Toast.makeText(this, "Message is empty!!", Toast.LENGTH_SHORT).show();
        }
    }

    class updateTwitterStatus extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ShareActivity.this);
            pDialog.setMessage("Posting to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected Void doInBackground(String... args) {

            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();

                builder.setOAuthConsumerKey(consumerKey);
                builder.setOAuthConsumerSecret(consumerSecret);


                // Access Token
                String access_token = mSharedPreferences.getString(SocialConstants.PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(SocialConstants.PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
                InputStream is = getResources().openRawResource(R.drawable.lakeside_view);
                statusUpdate.setMedia("test.jpg", is);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (TwitterException e) {
                Log.d("Failed to post!", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

			/* Dismiss the progress dialog after sharing */
            pDialog.dismiss();

            Toast.makeText(ShareActivity.this, "Posted to Twitter!", Toast.LENGTH_SHORT).show();

            // Clearing EditText field
            mShareEditText.setText("");
        }

    }
}
