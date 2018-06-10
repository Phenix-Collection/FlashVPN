package com.polestar.minesweeperclassic.Service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.polestar.minesweeperclassic.utils.MLogs;

/**
 * Created by guojia on 2018/3/23.
 */

public class DaemonService extends Service {

    private static final int NOTIFY_ID = 1001;
    private static final String WAKE_ACTION = "com.polestar.minesweeperclassic.wake";
    private final static int ALARM_INTERVAL = 60 * 60 * 1000;

    public static void startup(Context context) {
        try {
            context.startService(new Intent(context, DaemonService.class));
        }catch (Exception ex) {
            MLogs.e("DaemonService",  ex.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startup(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MLogs.e("DaemonService onCreate");
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            startService(new Intent(this, InnerService.class));
            startForeground(NOTIFY_ID, new Notification());
        }

        //发送唤醒广播来促使挂掉的UI进程重新启动起来
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(WAKE_ACTION);
        PendingIntent operation = PendingIntent.getBroadcast(this, 1111, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, operation);
        }catch (Exception e) {
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static final class InnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            MLogs.e("DaemonService onStartCommand");
            startForeground(NOTIFY_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


}
