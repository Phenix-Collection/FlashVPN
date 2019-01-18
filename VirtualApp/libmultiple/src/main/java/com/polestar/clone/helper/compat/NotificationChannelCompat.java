package com.polestar.clone.helper.compat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by guojia on 2019/1/18.
 */
public class NotificationChannelCompat {
    public static final String DEFAULT_CHANNEL_ID = "clone_app_default_msg";

    public NotificationChannelCompat() {
        super();
    }

    @TargetApi(26)
    public static void checkOrCreateChannel(Context arg4, String arg5, String arg6) {
        if(Build.VERSION.SDK_INT >= 26) {
            Object v0 = arg4.getSystemService(Context.NOTIFICATION_SERVICE);
            if(((NotificationManager)v0).getNotificationChannel(arg5) != null) {
                return;
            }

            NotificationChannel v1 = new NotificationChannel(arg5, ((CharSequence)arg6), NotificationManager.IMPORTANCE_HIGH);
            v1.setDescription("Compatibility of old versions");
            v1.setSound(null,null);
            v1.setShowBadge(false);
            try {
                ((NotificationManager)v0).createNotificationChannel(v1);
            }
            catch(Throwable v0_1) {
                v0_1.printStackTrace();
            }
        }
    }

    public static Notification.Builder createBuilder(Context arg2, String arg3) {
        Notification.Builder v0 = (Build.VERSION.SDK_INT < 26 || arg2.getApplicationInfo().targetSdkVersion < 26)
                ? new Notification.Builder(arg2) : new Notification.Builder(arg2, arg3);
        return v0;
    }

    public static boolean enable() {
        return true;
    }
}

