package com.polestar.booster;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.polestar.booster.mgr.BoostMgr;
import com.polestar.booster.util.AndroidUtil;
import com.polestar.booster.util.TimeUtil;

import static com.polestar.booster.BoosterSdk.PREF_NAME;


public class Booster extends Service {

    public static final String ACTION_INIT = BuildConfig.APPLICATION_ID + ".INIT";
    public static final String ACTION_WAKE = BuildConfig.APPLICATION_ID + ".WAKE";
    public static final String ACTION_CLEAN_SHORTCUT_CLICK = BuildConfig.APPLICATION_ID + ".CLEAN_SHORTCUT_CLICK";
    private static final int NOTIFY_ID = 1001;

    private volatile Looper mCleanerLooper;
    private volatile CleanerHandler mCleanerHandler;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    boolean mInitialized = false;

    private boolean mRedelivery;

//    HandlerTimer mSyncConfigInfoTimer;
//
//    static MinIntervalControl sSyncConfigInfoControl;
//
//    static MinIntervalControl getSyncConfigInfoControl(Context context) {
//        if (sSyncConfigInfoControl != null)
//            return sSyncConfigInfoControl;
//
//        sSyncConfigInfoControl = new MinIntervalControl(sp(context), PREF_KEY_LAST_SYNC_CONFIG_INFO_SUCCESS_TIME, TimeUtil.HOUR * 6L);
//        return sSyncConfigInfoControl;
//    }

    private final class CleanerHandler extends Handler {

        public CleanerHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                onHandleIntent((Intent) msg.obj);
        }
    }

    public Booster() {
        super();
    }

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void startInit(Context context) {
        try {
            Intent intent = new Intent(context, Booster.class);
            intent.setAction(ACTION_INIT);
            context.startService(intent);
        } catch (Exception e) {

        }
    }

    public static void wake(Context context, String src){
        try {
            Intent intent = new Intent(context, Booster.class);
            intent.setAction(ACTION_WAKE);
            intent.putExtra("wake_src", src);
            context.startService(intent);
        } catch (Exception e) {

        }
    }

    public static void startCleanShortcutClick(Context context, String from) {
        try {
            Intent intent = new Intent(context, Booster.class);
            intent.setAction(ACTION_CLEAN_SHORTCUT_CLICK);
            intent.putExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM, from);
            context.startService(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();

        HandlerThread thread = new HandlerThread("Booster", Thread.MIN_PRIORITY);
        thread.start();

        mCleanerLooper = thread.getLooper();
        mCleanerHandler = new CleanerHandler(mCleanerLooper);

//        mSyncConfigInfoTimer = new HandlerTimer(mCleanerHandler, new HandlerTimer.Task() {
//            @Override
//            public boolean run() {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        MinIntervalControl control = getSyncConfigInfoControl(context);
//                        if (control.check()) {
//                            syncConfigInfo();
//                        } else {
//                        }
//                    }
//                }.start();
//                return false;
//            }
//        }, TimeUtil.HOUR * 1L);

        mInitialized = false;

//        registerLockscreenReceiver();

        //schedule(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mCleanerHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mCleanerHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        BoosterLog.log("Booster onStart: " + intent);
        String wake = intent != null? intent.getStringExtra("wake_src"): "svc_restart";
        if (!TextUtils.isEmpty(wake)){
            BoosterLog.reportWake(wake);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && BoosterSdk.boosterConfig.showNotification
                && !getPackageName().endsWith("arm64")) {
            Intent start = getPackageManager().getLaunchIntentForPackage(getPackageName());
            start.addCategory(Intent.CATEGORY_LAUNCHER);
            start.setAction(Intent.ACTION_MAIN);
            //start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //start.setClass(this, )


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channel_id = "_id_service_";
            if (notificationManager.getNotificationChannel(channel_id) == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Clone App Messaging", importance);
//                notificationChannel.enableVibration(false);
                notificationChannel.enableLights(false);
//                notificationChannel.setVibrationPattern(new long[]{0});
                notificationChannel.setSound(null, null);
                notificationChannel.setDescription("Clone App Messaging & Notification");
                notificationChannel.setShowBadge(false);
                //notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(notificationChannel);
            }
            Notification.Builder mBuilder =  new Notification.Builder(this, channel_id);
            mBuilder.setContentTitle(getString(R.string.daemon_notification_text))
                    .setContentText(getString(R.string.daemon_notification_detail))
                    .setSmallIcon(this.getResources().getIdentifier("ic_launcher", "mipmap", this.getPackageName()))
                    .setContentIntent(PendingIntent.getActivity(this,0, start, 0));
            Notification notification = mBuilder.build();
            startForeground(NOTIFY_ID, notification);
        }
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        mSyncConfigInfoTimer.stop();

        mCleanerLooper.quit();

//        unregisterLockscreenReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        final String action = intent.getAction();
        try {
            if (ACTION_INIT.equals(action)) {
                handleInit();
                return;
            }
            if (ACTION_CLEAN_SHORTCUT_CLICK.equals(action)) {
                handleCleanShortcutClick(intent.getStringExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM));
                return;
            }
        } catch (Exception e) {
        } finally {
        }
    }

    private void handleInit() {
        if (mInitialized)
            return;

        mInitialized = true;
//        onConfigUpdated(this);
    }

    private void handleCleanShortcutClick(final String from) {

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onCleanShortcutClick(from);
            }
        });
    }


    private void onCleanShortcutClick(final String from) {

        BoostMgr.getInstance(this).onCleanShortcutClick(from);
    }

}
