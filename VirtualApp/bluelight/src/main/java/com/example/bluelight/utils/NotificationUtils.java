package com.example.bluelight.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.example.bluelight.R;
import com.example.bluelight.service.FilterService;
import com.example.bluelight.ui.MainActivity;
import com.example.bluelight.ui.TorchActivity;

public class NotificationUtils {
    private static final String CHANNEL_ID = "default";
    private static final int NOTIFICATION_ID = 1;

    public NotificationUtils() {
        super();
    }

    public static void cancelNotification(Context arg2) {
        NotificationManagerCompat.from(arg2).cancel(NOTIFICATION_ID);
    }

    public static void createNotification(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        NotificationUtils.createNotificationChannel(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        int v2 = ContextCompat.getColor(context, R.color.notification_on);
        int v3 = ContextCompat.getColor(context, R.color.notification_off);
        int v5 = R.id.notification_status_image;
        int v6 = R.id.notification_status_text;
        if (PreferenceUtils.isFilterEnable(context)) {
            views.setTextColor(v6, v2);
            views.setImageViewResource(v5, R.drawable.ic_night_mode_on);
        } else {
            views.setTextColor(v6, v3);
            views.setImageViewResource(v5, R.drawable.ic_night_mode_off);
        }

        v5 = R.id.notification_torch_image;
        v6 = R.id.notification_torch_text;
        if (PreferenceUtils.isTorchOn(context)) {
            views.setTextColor(v6, v2);
            views.setImageViewResource(v5, R.drawable.ic_torch_on);
        } else {
            views.setTextColor(v6, v3);
            views.setImageViewResource(v5, R.drawable.ic_torch_off);
        }

        views.setOnClickPendingIntent(R.id.notification_setting, PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0));
        Intent v7 = new Intent(context, FilterService.class);
        v7.setAction(FilterService.ACTION_TOGGLE_FILTER);
        views.setOnClickPendingIntent(R.id.notification_status, PendingIntent.getService(context, 0, v7, 0));
        views.setOnClickPendingIntent(R.id.notification_torch, PendingIntent.getActivity(context, 0, new Intent(context, TorchActivity.class), 0));
        Notification v10 = new NotificationCompat.Builder(context, CHANNEL_ID).
                setSmallIcon(R.drawable.notification_icon).
                setCustomContentView(views).
                setOnlyAlertOnce(true).
                build();
        v10.flags = 32;
        manager.notify(NOTIFICATION_ID, v10);
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            String v0 = context.getString(R.string.notification_channel_name);
            String v1 = context.getString(R.string.notification_channel_description);
            NotificationChannel v3 = new NotificationChannel(CHANNEL_ID, ((CharSequence) v0), NotificationManager.IMPORTANCE_DEFAULT);
            v3.setDescription(v1);
            context.getSystemService(NotificationManager.class).createNotificationChannel(v3);
        }
    }
}

