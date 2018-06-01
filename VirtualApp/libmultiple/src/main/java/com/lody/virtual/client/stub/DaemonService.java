package com.lody.virtual.client.stub;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;


/**
 * @author Lody
 */
public class DaemonService extends Service {

    private static final int NOTIFY_ID = 1001;
	private static final String WAKE_ACTION = VirtualCore.get().getHostPkg() + ".wake";
    private final static int ALARM_INTERVAL = 30 * 60 * 1000;

    public static void startup(Context context) {
		try {
			context.startService(new Intent(context, DaemonService.class));
		}catch (Exception ex) {
			VLog.e("DaemonService",  ex.toString());
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
        startService(new Intent(this, InnerService.class));
        startForeground(NOTIFY_ID, new Notification());

        //发送唤醒广播来促使挂掉的UI进程重新启动起来
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(WAKE_ACTION);
        PendingIntent operation = PendingIntent.getBroadcast(this, 1111, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, operation);
		}catch (Exception e) {
			VLog.logbug("Alarm", VLog.getStackTraceString(e));
		}

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static final class InnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            //  remoteViews = new RemoteViews(this.getPackageName(), R.layout.quick_switch_notification);
            Notification notification;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                Intent start = new Intent();
                start.addCategory(Intent.CATEGORY_LAUNCHER);
                start.setAction(Intent.ACTION_MAIN);
                start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                start.setPackage(this.getPackageName());
                Notification.Builder mBuilder =  new Notification.Builder(this);
                mBuilder.setContentTitle(getString(R.string.daemon_notification_text))
                        .setContentText(getString(R.string.daemon_notification_detail))
                        .setSmallIcon(this.getResources().getIdentifier("ic_launcher", "mipmap", this.getPackageName()))
                        .setContentIntent(PendingIntent.getActivity(this,0, start, 0));
                notification = mBuilder.build();
            } else {
                notification = new Notification();
            }
            startForeground(NOTIFY_ID, notification);
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
