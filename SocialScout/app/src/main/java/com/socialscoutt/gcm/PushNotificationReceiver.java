package com.socialscoutt.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.socialscoutt.R;
import com.socialscoutt.ShareActivity;
import com.socialscoutt.utils.SocialConstants;

public class PushNotificationReceiver extends BroadcastReceiver
{
	public static final String LOG_TAG = PushNotificationReceiver.class.getName();
	public static final String TOPIC_TITLE = "title";
	public static final String TOPIC_ID = "topicId";

    private Context context;


	@Override
	public void onReceive(final Context context, final Intent intent)
	{
        this.context = context;
		if ( intent != null )
		{
			final Bundle extras = intent.getExtras();
			this.processNotification(extras, context);
		}
	}


	public void processNotification(final Bundle extras, final Context context)
	{
		for ( final String key : extras.keySet() )
		{
			final String value = extras.getString(key);
			final String line = String.format("%s=%s", key, value);
			Log.i("received message", line);
			if ( "data".equals(key) )
			{
				// Continue to grow this as more features come under push notification (sg)
				final JsonObject notificationMessage = new JsonParser().parse(value).getAsJsonObject();
				final JsonElement titleElement = notificationMessage.get(TOPIC_TITLE);
				final JsonElement topicIdElement = notificationMessage.get(TOPIC_ID);
                if(null != titleElement && !titleElement.isJsonNull()) {
                    postNotification(titleElement.getAsString(), topicIdElement.getAsString());
                }
			}
		}
	}


	protected void postNotification(String smallMessage, String topicId)
	{
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.context).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle("New topic posted");
        mBuilder.setLights(R.color.col_notification, 500, 500);
        final NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(smallMessage);
        mBuilder.setStyle(style);

        final Intent resultIntent = new Intent(this.context, ShareActivity.class);
        resultIntent.putExtra(SocialConstants.TOPIC_ID, topicId);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(this.context, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        // mBuilder.setAutoCancel(true);
        final NotificationManager mNotificationManager = (NotificationManager)this.context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
	}
}
