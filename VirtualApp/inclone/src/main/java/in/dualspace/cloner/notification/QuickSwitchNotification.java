package in.dualspace.cloner.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.BitmapUtils;
import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.BuildConfig;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.components.ui.AppLoadingActivity;
import in.dualspace.cloner.components.ui.HomeActivity;
import in.dualspace.cloner.db.CloneModel;
import com.polestar.clone.CustomizeAppData;
import in.dualspace.cloner.db.DBManager;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.utils.ResourcesUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Created by guojia on 2017/12/23.
 */

public class QuickSwitchNotification {

    private static QuickSwitchNotification sInstance;
    private NotificationManager mgr;
    private Context mContext;

    private final ArrayList<String> lruKeys = new ArrayList<>();
    private Handler workHandler;
    private Handler mainHandler;
    private static final String CONFIG_HOT_CLONE_LIST = "hot_clone_list";
    private static final String PREF_QUICK_SWITCH_STATE = "quick_switch_state";
    private static final int FAKE_USERID_FOR_UNCLONED = 999;

    private static final int LRU_PACKAGE_CNT = 4;

    private static final String SPLIT = ";";

    private static final int NOTIFY_ID = 999;
    private RemoteViews remoteViews;
    private static final String TAG = "QuickSwitchNotification";
    private static final String ACTION_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".quick_switch";
    private static final String ACTION_CANCEL_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".cancel_quick_switch";
    private static final String ACTION_ENABLE_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".enable_quick_switch";
    private static final String EXTRA_START_PACKAGE = "extra_start_package";
    private static final String EXTRA_START_USERID = "extra_start_userid";
    private static final String CATEGORY_NOTIFY = "cat_notify";
    private static final String CATEGORY_ENABLE = "cat_enable";
    private boolean isInitialized = false;

    private QuickSwitchNotification(Context ctx){
        mContext = ctx.getApplicationContext();
        mgr = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        HandlerThread thread = new HandlerThread("switch_worker");
        thread.start();
        workHandler = new Handler(thread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_QUICK_SWITCH);
        intentFilter.addAction(ACTION_CANCEL_QUICK_SWITCH);
        intentFilter.addAction(ACTION_ENABLE_QUICK_SWITCH);
        intentFilter.addCategory(CATEGORY_ENABLE);
        intentFilter.addCategory(CATEGORY_NOTIFY + 0);
        intentFilter.addCategory(CATEGORY_NOTIFY + 1);
        intentFilter.addCategory(CATEGORY_NOTIFY + 2);
        intentFilter.addCategory(CATEGORY_NOTIFY + 3);
        mContext.registerReceiver(new SwitchIntentReceiver(), intentFilter);
    }

    public void init() {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                readLruKeys();
                isInitialized = true;
                updateLruPackages(null);
            }
        });
    }
    private void readLruKeys() {
        String data = PreferencesUtils.getString(mContext, "lru_pkg");
        synchronized (lruKeys) {
            if (!TextUtils.isEmpty(data)) {
                String arr[] = data.split(SPLIT);
                if (arr != null && arr.length != 0) {
                    for (String s : arr) {
                        if (!TextUtils.isEmpty(s)) {
                            String pkg = CloneManager.getNameFromKey(s);
                            int userId = CloneManager.getUserIdFromKey(s);
                            if (!lruKeys.contains(s) && VirtualCore.get().isAppInstalledAsUser(userId, pkg)) {
                                lruKeys.add(s);
                            }
                        }
                    }
                }
            }
            if (lruKeys.size() <LRU_PACKAGE_CNT) {
                List<CloneModel> list = DBManager.queryAppList(DualApp.getApp());
                for(CloneModel app:list) {
                    String key = CloneManager.getMapKey(app.getPackageName(), app.getPkgUserId());
                    if (!lruKeys.contains(key))
                        lruKeys.add(key);
                }
            }
            //Reserve one + slot iff not enough clones
            if (lruKeys.size() < (LRU_PACKAGE_CNT - 1)) {
                String hotCloneConf = RemoteConfig.getString(CONFIG_HOT_CLONE_LIST);
                HashSet<String> hotCloneSet = new HashSet<>();
                if (!TextUtils.isEmpty(hotCloneConf)) {
                    String[] arr = hotCloneConf.split(":");
                    for (String s : arr) {
                        hotCloneSet.add(s);
                        try {
                            ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(s, 0);
                            if (ai != null) {
                                //hack for apps not cloned
                                String key = CloneManager.getMapKey(s, FAKE_USERID_FOR_UNCLONED);
                                if (!lruKeys.contains(key)) {
                                    lruKeys.add(key);
                                }
                            }
                            if (lruKeys.size() >= (LRU_PACKAGE_CNT-1)) {
                                return;
                            }
                        } catch (Exception ex) {

                        }
                    }
                }
            }
        }
        MLogs.d(TAG,"readLruKeys");
        dumpLru();
    }

    private void writeLruKeys() {
        String data = "";
        synchronized (lruKeys) {
            for (String s : lruKeys) {
                data += s;
                data += SPLIT;
            }
        }
        PreferencesUtils.putString(mContext, "lru_pkg", data);
        MLogs.d(TAG,"writeLruKeys");
        dumpLru();
    }

    public synchronized static QuickSwitchNotification getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new QuickSwitchNotification(context);
        }
        return sInstance;
    }

    private Runnable writeLruRunnable = new Runnable() {
        @Override
        public void run() {
            writeLruKeys();
        }
    };

    private int getTitleIdForItem(int index) {
        switch (index) {
            case 0:
                return R.id.shortcut_txt0;
            case 1:
                return R.id.shortcut_txt1;
            case 2:
                return R.id.shortcut_txt2;
            case 3:
                return R.id.shortcut_txt3;
        }
        return 0;
    }

    private int getIconIdForItem(int index) {
        switch (index) {
            case 0:
                return R.id.shortcut_icon0;
            case 1:
                return R.id.shortcut_icon1;
            case 2:
                return R.id.shortcut_icon2;
            case 3:
                return R.id.shortcut_icon3;
        }
        return 0;
    }

    private void dumpLru() {
        if (BuildConfig.DEBUG) {
            synchronized (lruKeys) {
                for (String s : lruKeys) {
                    MLogs.d(TAG, "LRU " + s);
                }
            }
        }
    }
    private void updateNotification() {
        MLogs.d(TAG,"updateNotification");
        dumpLru();
        if (remoteViews == null) {
            remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.quick_switch_notification);
        }
        String channel_id = "_id_quick_switch_";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager.getNotificationChannel(channel_id) == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Quick Switch", importance);
//                notificationChannel.enableVibration(false);
                notificationChannel.enableLights(false);
//                notificationChannel.setVibrationPattern(new long[]{0});
                notificationChannel.setSound(null, null);
                notificationChannel.setDescription("Quick Switch Shortcuts");
                notificationChannel.setShowBadge(false);
                //notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews).setOngoing(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(channel_id)
                .setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext,HomeActivity.class), 0));
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT;
        notification.priority = Notification.PRIORITY_MAX;
//
//        Intent boostIntent = new Intent(mContext, BoosterShortcutActivity.class);
//        boostIntent.setAction(BoosterShortcutActivity.class.getName());
//        boostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        boostIntent.putExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM, "notification");
//        remoteViews.setOnClickPendingIntent(R.id.booster_icon,PendingIntent.getActivity(mContext, 0, boostIntent,0));
//
        synchronized (lruKeys) {
            for (int i = 0; i < LRU_PACKAGE_CNT; i++) {
                if (lruKeys.size() <= i) {

                } else {
                    int titleId = getTitleIdForItem(i);
                    int iconId = getIconIdForItem(i);
                    if (titleId != 0 && iconId != 0) {
                        String mapKey = lruKeys.get(i);
                        String pkg = CloneManager.getNameFromKey(mapKey);
                        int userId = CloneManager.getUserIdFromKey(mapKey);
                        if (VirtualCore.get().isAppInstalledAsUser(userId, pkg)) {
                            CustomizeAppData data = CustomizeAppData.loadFromPref(pkg, userId);
                            remoteViews.setImageViewBitmap(iconId, data.getCustomIcon());
                            if (!data.customized) {
                                remoteViews.setTextViewText(titleId, String.format(ResourcesUtil.getString(R.string.clone_label_tag),
                                        CloneManager.getInstance(DualApp.getApp()).getModelName(pkg, userId)));
                            } else {
                                remoteViews.setTextViewText(titleId, data.label);
                            }
                            Intent intent = new Intent(ACTION_QUICK_SWITCH);
                            intent.addCategory(CATEGORY_NOTIFY + i);
                            intent.putExtra(EXTRA_START_PACKAGE, pkg);
                            intent.putExtra(EXTRA_START_USERID, userId);
                            MLogs.d(TAG, "Pending intent pkg: " + pkg + " userId:" + userId);
                            remoteViews.setOnClickPendingIntent(iconId,
                                    PendingIntent.getBroadcast(mContext, i, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                        } else {
                            try {
                                ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(pkg, 0);
                                if (ai != null) {
                                    remoteViews.setImageViewBitmap(iconId, BitmapUtils.drawableToBitmap(ai.loadIcon(mContext.getPackageManager())));
                                    remoteViews.setTextViewText(titleId, ai.loadLabel(mContext.getPackageManager()));
                                    Intent intent = new Intent(ACTION_QUICK_SWITCH);
                                    intent.addCategory(CATEGORY_NOTIFY + i);
                                    intent.putExtra(EXTRA_START_PACKAGE, pkg);
                                    intent.putExtra(EXTRA_START_USERID, userId);
                                    remoteViews.setOnClickPendingIntent(iconId,
                                            PendingIntent.getBroadcast(mContext, i, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                }
                            } catch (Exception ex) {

                            }
                        }
                    }
                }
            }
        }
        mgr.notify(NOTIFY_ID, notification);
    }

    public void updateLruPackages(String mapKey) {
        MLogs.d(TAG,"updateLruPackages " + mapKey);
        if (! isInitialized ) {
            return;
        }
        if (!TextUtils.isEmpty(mapKey)) {
            synchronized (lruKeys) {
                if (lruKeys.contains(mapKey)) {
                    return;
                }
                if (!TextUtils.isEmpty(mapKey)) {
                    if (lruKeys.size() < LRU_PACKAGE_CNT) {
                        lruKeys.add(mapKey);
                    } else {
                        lruKeys.remove(0);
                        lruKeys.add(mapKey);
                    }
                }
            }
        }
        workHandler.removeCallbacks(writeLruRunnable);
        workHandler.postDelayed(writeLruRunnable, 2000);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                updateNotification();
            }
        });
    }

    public static void enable(){
        PreferencesUtils.putInt(DualApp.getApp(), PREF_QUICK_SWITCH_STATE, STATE_ENABLE);
        Intent intent = new Intent(ACTION_ENABLE_QUICK_SWITCH);
        intent.addCategory(CATEGORY_ENABLE);
        VirtualCore.get().getContext().sendBroadcast(intent);
        EventReporter.quickSwitchSetting(true);
    }

    public static void disable() {
        PreferencesUtils.putInt(DualApp.getApp(), PREF_QUICK_SWITCH_STATE, STATE_DISABLE);
        Intent intent = new Intent(ACTION_CANCEL_QUICK_SWITCH);
        intent.addCategory(CATEGORY_ENABLE);
        VirtualCore.get().getContext().sendBroadcast(intent);
        EventReporter.quickSwitchSetting(false);
    }

    class SwitchIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MLogs.d(TAG, "onReceive " + intent);
            if (intent.getAction().equals(ACTION_CANCEL_QUICK_SWITCH)) {
                mgr.cancel(NOTIFY_ID);
                isInitialized = false;
            } else if (intent.getAction().equals(ACTION_ENABLE_QUICK_SWITCH)) {
                init();
            } else {
                String pkg = intent.getStringExtra(EXTRA_START_PACKAGE);
                int userId = intent.getIntExtra(EXTRA_START_USERID, 0);
                MLogs.d(TAG, "onReceive " + pkg + " user " + userId);
                startApp(pkg, userId);
            }
        }
    }

    private void startApp(String pkg, int userId) {
        if (VirtualCore.get().isAppInstalledAsUser(userId, pkg)) {
            MLogs.d(TAG, "startApp for cloned pkg" + pkg);
            if (CloneManager.isAppRunning(pkg, userId)) {
                CloneManager.launchApp(null, pkg, userId, false);
            } else {
                Intent intent = new Intent(mContext, AppLoadingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, pkg);
                intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_NOTIFY);
                intent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, userId);
                mContext.startActivity(intent);
            }
        } else {
            MLogs.d(TAG, "startApp for not cloned pkg" + pkg);
            Intent intent = VirtualCore.get().getUnHookPackageManager().getLaunchIntentForPackage(pkg);
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setPackage(pkg);
            if (intent != null) {
                mContext.startActivity(intent);
            } else {
                synchronized (lruKeys) {
                    lruKeys.remove(CloneManager.getMapKey(pkg, FAKE_USERID_FOR_UNCLONED));
                    updateLruPackages(null);
                }
            }
        }

    }


    public static final int STATE_NOT_SET = -1;
    public static final int STATE_DISABLE = 0;
    public static final int STATE_ENABLE = 1;
    public static int getQuickSwitchState() {
        return  PreferencesUtils.getInt(DualApp.getApp(), "quick_switch_state", -1);
    }
    public static boolean isEnable() {
        int state = getQuickSwitchState();
        MLogs.d(TAG+" is enable state: " + state);
        if (state == STATE_NOT_SET) {
            return RemoteConfig.getBoolean("default_enable_quick_switch");
        } else {
            return state == STATE_ENABLE;
        }
    }
}
