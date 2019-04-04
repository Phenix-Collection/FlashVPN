package com.polestar.superclone.notification;

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
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.polestar.clone.BitmapUtils;
import com.polestar.clone.CustomizeAppData;
import com.polestar.clone.GmsSupport;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.stub.DaemonService;
import com.polestar.superclone.BuildConfig;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.activity.AppStartActivity;
import com.polestar.superclone.component.activity.HomeActivity;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.utils.AppManager;
import com.polestar.superclone.utils.CloneHelper;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.superclone.utils.ResourcesUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by guojia on 2019/3/19.
 */

public class FastSwitch {
    private static FastSwitch sInstance;
    private NotificationManager mgr;
    private Context mContext;

    private final ArrayList<String> lruKeys = new ArrayList<>();
    private Handler workHandler;
    private static final String PREF_QUICK_SWITCH_STATE = "quick_switch_state";
    private static final int FAKE_USERID_FOR_UNCLONED = 999;

    public static final int LRU_PACKAGE_CNT = 4;

    private static final String SPLIT = ";";

    private static final int NOTIFY_ID = DaemonService.NOTIFY_ID;
    private RemoteViews remoteViews;
    private static final String TAG = "FastSwitch";
    private static final String ACTION_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".quick_switch";
    private static final String ACTION_CANCEL_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".cancel_quick_switch";
    private static final String ACTION_ENABLE_QUICK_SWITCH = BuildConfig.APPLICATION_ID + ".enable_quick_switch";
    private static final String EXTRA_START_PACKAGE = "extra_start_package";
    private static final String EXTRA_START_USERID = "extra_start_userid";
    private static final String CATEGORY_NOTIFY = "cat_notify";
    private static final String CATEGORY_ENABLE = "cat_enable";
    private boolean isInitialized = false;

    private FastSwitch(Context ctx){
        mContext = ctx.getApplicationContext();
        mgr = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        HandlerThread thread = new HandlerThread("switch_worker");
        thread.start();
        workHandler = new Handler(thread.getLooper());
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
                MLogs.d("Initialized");
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
                        if (lruKeys.size() >= LRU_PACKAGE_CNT) {
                            break;
                        }
                        if (!TextUtils.isEmpty(s)) {
                            if (!lruKeys.contains(s)) {
                                lruKeys.add(s);
                            }
                        }
                    }
                }
            }
            paddingLruKeys();
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

    public synchronized static FastSwitch getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FastSwitch(context.getApplicationContext());
        }
        return sInstance;
    }

    private Runnable writeLruRunnable = new Runnable() {
        @Override
        public void run() {
            writeLruKeys();
        }
    };

    private void scheduleUpdateNotification() {
        workHandler.removeCallbacks(updateNotificationRunnable);
        workHandler.postDelayed(updateNotificationRunnable, 1000);
    }

    private void scheduleSaveLru() {
        workHandler.removeCallbacks(writeLruRunnable);
        workHandler.postDelayed(writeLruRunnable, 2000);
    }

    private Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
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
//        String channel_id = "_id_quick_switch_";
        String channel_id = DaemonService.NOTIFICATION_CHANNEL_ID;
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
        Intent mainIntent = new Intent(mContext, HomeActivity.class);
        mainIntent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_NOFITIFCATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext,channel_id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews).setOngoing(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(PendingIntent.getActivity(mContext, 0, mainIntent, 0));
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
                int titleId = getTitleIdForItem(i);
                int iconId = getIconIdForItem(i);
                if (i >= lruKeys.size()) {
                    MLogs.d(TAG, "Empty slot: " + i);
                    remoteViews.setImageViewResource(iconId, R.drawable.icon_add);
                    remoteViews.setTextViewText(titleId, "");
                } else {
                    if (titleId != 0 && iconId != 0) {
                        String mapKey = lruKeys.get(i);
                        String pkg = AppManager.getNameFromKey(mapKey);
                        int userId = AppManager.getUserIdFromKey(mapKey);
                        if (isAppCloned(pkg, userId)) {
                            CustomizeAppData data = CustomizeAppData.loadFromPref(pkg, userId);
                            remoteViews.setImageViewBitmap(iconId, data.getCustomIcon());
                            if (!data.customized) {
                                remoteViews.setTextViewText(titleId, String.format(ResourcesUtil.getString(R.string.clone_label_tag),
                                        AppManager.getModelName(pkg, userId)));
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
        try {
            mgr.cancel(NOTIFY_ID);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        mgr.notify(NOTIFY_ID, notification);
    }

    private void paddingLruKeys() {
        Iterator<String> it = lruKeys.iterator();
        while(it.hasNext()){
            String x = it.next();
            String pkg = AppManager.getNameFromKey(x);
            int userId = AppManager.getUserIdFromKey(x);
            if (!isAppCloned(pkg, userId)) {
                it.remove();
            }
        }
        if (lruKeys.size() < LRU_PACKAGE_CNT) {
            List<AppModel> list = DbManager.queryAppList(MApp.getApp());
            for(AppModel app:list) {
                String key = AppManager.getMapKey(app.getPackageName(), app.getPkgUserId());
                if (lruKeys.size() >= LRU_PACKAGE_CNT) {
                    break;
                }
                if (!lruKeys.contains(key)) {
                    lruKeys.add(key);
                    MLogs.d(TAG, "add clone: " + key);
                }
            }
            //force update remote view;
            MLogs.d(TAG, "clear remote view");

            remoteViews = null;
        }
        //Reserve one + slot iff not enough clones
        if (lruKeys.size() < (LRU_PACKAGE_CNT - 1)) {
            String hotCloneConf = RemoteConfig.getString("conf_social_app_list");
            if (!TextUtils.isEmpty(hotCloneConf)) {
                String[] arr = hotCloneConf.split(":");
                for (String s : arr) {
                    try {
                        ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(s, 0);
                        if (ai != null) {
                            //hack for apps not cloned
                            String key = AppManager.getMapKey(s, FAKE_USERID_FOR_UNCLONED);
                            if (!lruKeys.contains(key)) {
                                lruKeys.add(key);
                                MLogs.d(TAG, "add hot: " + key);

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
    public void removeLruPackages(String mapKey) {
        MLogs.d(TAG,"removeLruPackages " + mapKey);
        if (! isInitialized ) {
            return;
        }
        if (!TextUtils.isEmpty(mapKey)) {
            synchronized (lruKeys) {
                lruKeys.remove(mapKey);
                paddingLruKeys();
            }
        }
        scheduleSaveLru();
        scheduleUpdateNotification();
    }

    public void updateLruPackages(String mapKey) {
        MLogs.d(TAG,"updateLruPackages " + mapKey);
        if (! isInitialized ) {
            MLogs.d(TAG,"not init");
            return;
        }
        if (!TextUtils.isEmpty(mapKey)) {
            String name = AppManager.getNameFromKey(mapKey);
            if (VirtualCore.isPreInstalledPkg(name)
                    || MApp.getApp().getPackageName().equals(name)){
                return;
            }
            synchronized (lruKeys) {
                if (lruKeys.contains(mapKey)) {
                    return;
                }
                if (!TextUtils.isEmpty(mapKey)) {
                    if (lruKeys.size() < LRU_PACKAGE_CNT) {
                        lruKeys.add(0, mapKey);
                    } else {
                        lruKeys.remove(lruKeys.size() - 1);
                        lruKeys.add(0, mapKey);
                    }
                }
                paddingLruKeys();
            }
        }
        scheduleSaveLru();
        scheduleUpdateNotification();
    }

    public static void enable(){
        FastSwitch.getInstance(MApp.getApp());
        PreferencesUtils.putInt(MApp.getApp(), PREF_QUICK_SWITCH_STATE, STATE_ENABLE);
        Intent intent = new Intent(ACTION_ENABLE_QUICK_SWITCH);
        intent.addCategory(CATEGORY_ENABLE);
        VirtualCore.get().getContext().sendBroadcast(intent);
        EventReporter.setUserProperty(EventReporter.PROP_FAST_SWITCH, "enable");
        EventReporter.generalEvent("enable_quick_switch");
    }

    public static void disable() {
        FastSwitch.getInstance(MApp.getApp());
        PreferencesUtils.putInt(MApp.getApp(), PREF_QUICK_SWITCH_STATE, STATE_DISABLE);
        Intent intent = new Intent(ACTION_CANCEL_QUICK_SWITCH);
        intent.addCategory(CATEGORY_ENABLE);
        VirtualCore.get().getContext().sendBroadcast(intent);
        EventReporter.setUserProperty(EventReporter.PROP_FAST_SWITCH, "disable");
        EventReporter.generalEvent("disable_quick_switch");
    }

    class SwitchIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MLogs.d(TAG, "onReceive " + intent);
            if (intent.getAction().equals(ACTION_CANCEL_QUICK_SWITCH)) {
                mgr.cancel(NOTIFY_ID);
                DaemonService.updateNotification(context);
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

    private boolean isAppCloned(String pkg, int userId) {
        return  VirtualCore.get().isAppInstalledAsUser(userId, pkg);
    }

    private void startApp(String pkg, int userId) {
        if (isAppCloned(pkg, userId)) {
            MLogs.d(TAG, "startApp for cloned pkg" + pkg);
      
            Intent intent = new Intent(mContext, AppStartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, pkg);
            intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_NOFITIFCATION);
            intent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, userId);
            mContext.startActivity(intent);
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
                    lruKeys.remove(AppManager.getMapKey(pkg, FAKE_USERID_FOR_UNCLONED));
                    updateLruPackages(null);
                }
            }
        }

    }

    public static final int STATE_NOT_SET = -1;
    public static final int STATE_DISABLE = 0;
    public static final int STATE_ENABLE = 1;
    public static int getQuickSwitchState() {
        return  PreferencesUtils.getInt(MApp.getApp(), "quick_switch_state", -1);
    }
    
    public static boolean isEnable() {
        if(MApp.isSupportPkg()) {
            return false;
        }
        int state = getQuickSwitchState();
        MLogs.d(TAG+" is enable state: " + state);
        if (state == STATE_NOT_SET) {
            return RemoteConfig.getBoolean("default_enable_quick_switch");
        } else {
            return state == STATE_ENABLE;
        }
    }
}
