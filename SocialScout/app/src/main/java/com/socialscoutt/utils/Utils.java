package com.socialscoutt.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by saurabhgangarde on 28/10/14.
 */
public class Utils {

    public static void discardNotification(final Context context, final int notificationId)
    {
        final Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
        final NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
        // As long as all notifications are taking to Dashboard, we can cancel all (sg)
        manager.cancelAll();
    }
}
