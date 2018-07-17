package com.example.bluelight.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.bluelight.service.FilterService;

import java.util.Calendar;

public class AlarmUtils {
    public AlarmUtils() {
        super();
    }

    public static void cancelAlarm(Context context) {
        Object v0 = context.getSystemService(Context.ALARM_SERVICE);
        if (v0 == null) {
            return;
        }

        ((AlarmManager) v0).cancel(AlarmUtils.getPendingIntent(context));
    }

    public static long getNextAlarmTime(Context context) {
        int startTime = PreferenceUtils.getAutoEnableStartTime(context);
        int stopTime = PreferenceUtils.getAutoEnableStopTime(context);
        int min = Math.min(startTime, stopTime);
        int max = Math.max(startTime, stopTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int v5 = 11;
        int v7 = 12;
        int currentTime = PreferenceUtils.formatTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        int time = 0;
        int day = 0;
        if (currentTime < min) {
            time = min;
        } else if (currentTime >= min && currentTime < max) {
            time = max;
        } else if (currentTime >= max) {


            time = min;
            day = 1;
        }

        calendar.set(Calendar.HOUR_OF_DAY, PreferenceUtils.getHour(time));
        calendar.set(Calendar.MINUTE, PreferenceUtils.getMinutes(time));
        if (day != 0) {
            calendar.add(Calendar.DATE, day);
        }

        return calendar.getTimeInMillis();
    }

    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, FilterService.class);
        intent.setAction(FilterService.ACTION_ALARM_EXPIRE);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    public static void setupAlarm(Context context) {
        Object v0 = context.getSystemService(Context.ALARM_SERVICE);
        if (v0 == null) {
            return;
        }

        PendingIntent pendingIntent = AlarmUtils.getPendingIntent(context);
        long time = AlarmUtils.getNextAlarmTime(context);
        if (Build.VERSION.SDK_INT >= 19) {
            ((AlarmManager) v0).setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            ((AlarmManager) v0).set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}

