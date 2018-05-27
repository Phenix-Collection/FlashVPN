package com.polestar.domultiple.widget.locker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.BuildConfig;
import com.polestar.domultiple.IAppMonitor;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.components.AppMonitorService;
import com.polestar.domultiple.components.ui.AppLockActivity;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.notification.QuickSwitchNotification;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by PolestarApp on 2017/1/4.
 */

public class AppLockMonitor {
    //lock interval 之内解过锁的不再锁
    //前后台切换超过lock interval的算一次switch
    //广告、界面相关逻辑移到AppMonitorService

    private String mLastForegroundPkg;
    private long mLastForegroundTime = 0;
    private String mUnlockedForegroundPkg;
    private static AppLockMonitor sInstance = null;
    private HashMap<String , CloneModel> modelHashMap = new HashMap<>();
    private Handler mHandler;
    private static long relockDelay = PreferencesUtils.getLockInterval(); //if paused for 2 minutes, and then resume, it need be locked
    private final static int MSG_DELAY_LOCK_APP = 0;
    public final static int MSG_PACKAGE_UNLOCKED = 1;
    public final static int MSG_PRELOAD_AD = 2;
    public final static int MSG_APP_FOREGROUND = 3;
    public final static int MSG_APP_BACKGROUND = 4;
    public final static String CONFIG_APPLOCK_PRELOAD_INTERVAL = "applock_preload_interval";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";
    private final static String TAG = "AppLockMonitor";

    private final static String ACTION_RELOAD_SETTING = BuildConfig.APPLICATION_ID + ".reload_lock";
    private final static String EXTRA_NEW_KEY = "extra_new_key";
    private final static String EXTRA_NEW_INTERVAL = "extra_new_interval";
    private final static String EXTRA_AD_FREE = "extra_ad_free";

    private FuseAdLoader mAdLoader;
    private boolean hasAppLocked;
    private boolean adFree;
    private IAppMonitor uiAgent;

    public static void updateSetting(String newKey, boolean adFree, long interval) {
        Intent intent = new Intent(ACTION_RELOAD_SETTING);
        intent.putExtra(EXTRA_NEW_KEY, newKey);
        intent.putExtra(EXTRA_NEW_INTERVAL, interval);
        intent.putExtra(EXTRA_AD_FREE, adFree);
        PolestarApp.getApp().sendBroadcast(intent);
    }

    class ReloadSettingReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            MLogs.d("ReloadSettingReceiver onReceive");
            reloadSetting (intent.getStringExtra(EXTRA_NEW_KEY),
                    intent.getBooleanExtra(EXTRA_AD_FREE, false),
                    intent.getLongExtra(EXTRA_NEW_INTERVAL, 3000));
        }
    }

    private AppLockMonitor() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RELOAD_SETTING);
        PolestarApp.getApp().registerReceiver(new ReloadSettingReceiver(), filter);
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DELAY_LOCK_APP:
                        MLogs.d(TAG, "Package change to background, last foreground: " + mUnlockedForegroundPkg);
                        QuickSwitchNotification.getInstance(VirtualCore.get().getContext()).updateLruPackages((String)msg.obj);
                        mUnlockedForegroundPkg = null;
                        break;
                    case MSG_PACKAGE_UNLOCKED:
                        String pkg = (String) msg.obj;
                        MLogs.d(TAG, "Package was unlocked" + pkg);
                        mUnlockedForegroundPkg = pkg;
                        break;
                    case MSG_PRELOAD_AD:
                        if (!adFree && hasLocker()) {
                            if (mAdLoader.hasValidAdSource()) {
                                mAdLoader.loadAd(1, null);
                            }
                            long interval = RemoteConfig.getLong(CONFIG_APPLOCK_PRELOAD_INTERVAL);
                            MLogs.d(TAG, "Applocker schedule next ad at " + interval);
                            if (interval >= 15*60*000) {
                                MLogs.d(TAG, "Go schedule next ad at " + interval);
                                mHandler.sendEmptyMessageDelayed(MSG_PRELOAD_AD, interval);
                            }
                        }
                    case MSG_APP_FOREGROUND:
                        String key = (String) msg.obj;
                        try {
                            String name = CloneManager.getNameFromKey(key);
                            int userId = CloneManager.getUserIdFromKey(key);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        if(getAgent() != null) {
                                            getAgent().onAppLock(name, userId);
                                        }
                                    }catch (Exception ex) {
                                        MLogs.logBug(ex);
                                    }
                                }
                            }).start();
                            AppLockActivity.start(VirtualCore.get().getContext(), name, userId);
                        }catch (Exception ex) {

                        }
                        break;
                    case MSG_APP_BACKGROUND:
                        break;
                }
            }
        };
        initSetting();
    }

    private boolean hasLocker() {
        return (hasAppLocked || PolestarApp.isSupportPkg())
                && !TextUtils.isEmpty(LockPatternUtils.getTempKey());
    }
    private void preloadAd() {
        mHandler.removeMessages(MSG_PRELOAD_AD);
        mHandler.sendEmptyMessage(MSG_PRELOAD_AD);
    }

    private void initSetting() {
        MLogs.d("initSetting");
        if (!PolestarApp.isSupportPkg()) {
            List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
            for (CloneModel model : list) {
                modelHashMap.put(CloneManager.getMapKey(model.getPackageName(), model.getPkgUserId()), model);
                if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                    hasAppLocked = true;
                }
            }
            adFree = false;
            mAdLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, PolestarApp.getApp());
            mAdLoader.setBannerAdSize(AppLockActivity.getBannerSize());
            preloadAd();
        }
        LockPatternUtils.setTempKey(PreferencesUtils.getEncodedPatternPassword(PolestarApp.getApp()));
    }

    public FuseAdLoader getAdLoader(){
        return mAdLoader;
    }

    public void reloadSetting(String newKey, boolean adFree, long interval) {
        MLogs.d(TAG, "reloadSetting adfree:" + adFree + " tmpkey: " + newKey);
        modelHashMap.clear();
        if (!PolestarApp.isSupportPkg()) {
            DBManager.resetSession();
            List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
            for (CloneModel model : list) {
                modelHashMap.put(CloneManager.getMapKey(model.getPackageName(), model.getPkgUserId()), model);
                if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                    MLogs.d(TAG, "hasAppLocked " + model.getPackageName());
                    hasAppLocked = true;
                }
            }
            preloadAd();
        }
        this.adFree = adFree;
        LockPatternUtils.setTempKey(newKey);
        if ( interval >= 3000) {
            relockDelay = interval;
        }
    }

    public static synchronized AppLockMonitor getInstance(){
        if (sInstance == null) {
            sInstance = new AppLockMonitor();
        }
        return sInstance;
    }

    public void unlocked(String pkg, int userId) {
        mHandler.sendMessage(mHandler.obtainMessage(AppLockMonitor.MSG_PACKAGE_UNLOCKED, CloneManager.getMapKey(pkg, userId)));
    }

    public void onActivityResume(String pkg, int userId) {
        String key = CloneManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityResume " + key);
        CloneModel model = modelHashMap.get(key);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + pkg);
            return;
        }
        if (hasLocker() && model.getLockerState() != AppConstants.AppLockState.DISABLED) {
            MLogs.d(TAG, "Need lock app " + pkg);
            if (mUnlockedForegroundPkg == null || (!mUnlockedForegroundPkg.equals(key))) {
                //do lock
                MLogs.d(TAG, "Do lock app " + pkg);
                mHandler.removeMessages(MSG_APP_FOREGROUND, key);
                mHandler.removeMessages(MSG_APP_BACKGROUND, key);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_APP_FOREGROUND, key));
            }
        }
        //Remove the same object with send
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, key);
        if (!key.equals(mLastForegroundPkg)
                || (System.currentTimeMillis() - mLastForegroundTime) > relockDelay) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getAgent() != null) {
                            getAgent().onAppSwitchForeground(pkg, userId);
                        }
                    }catch (Exception ex){
                        MLogs.logBug(ex);
                    }
                }
            }).start();
        }
        //mUnlockedForegroundPkg = pkg;
    }

    public void onActivityPause(String pkg, int userId) {
        String key = CloneManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityPause " + pkg + " delay relock: " + relockDelay);
        CloneModel model = modelHashMap.get(key);
        if (model != null) {
            mHandler.removeMessages(MSG_APP_FOREGROUND, key);
            mHandler.removeMessages(MSG_APP_BACKGROUND, key);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_APP_BACKGROUND, key), 500);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, key),
                relockDelay);
        mLastForegroundPkg = CloneManager.getMapKey(pkg, userId);
        mLastForegroundTime = System.currentTimeMillis();
    }

    private IAppMonitor getAgent() {
        if (uiAgent != null) {
            return  uiAgent;
        }
        String targetPkg = PolestarApp.getApp().getPackageName();
        if (targetPkg.endsWith(".arm64")) {
            targetPkg = targetPkg.replace(".arm64","");
        }
        try{
            ApplicationInfo ai = PolestarApp.getApp().getPackageManager().getApplicationInfo(targetPkg, 0);
        }catch (PackageManager.NameNotFoundException ex) {
            return  null;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("Cannot getAgent in main thread!");
        }
        ComponentName comp = new ComponentName(targetPkg, AppMonitorService.class.getName());
        Intent intent = new Intent();
        intent.setComponent(comp);
        VLog.d("CloneAgent", "bindService intent "+ intent);
        syncQueue.clear();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    syncQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 5000);
        PolestarApp.getApp().bindService(intent,
                agentServiceConnection,
                Context.BIND_AUTO_CREATE);
        try {
            syncQueue.take();
        }catch (Exception ex) {

        }
        return uiAgent;
    }

    private final BlockingQueue<Integer> syncQueue = new LinkedBlockingQueue<Integer>(1);
    ServiceConnection agentServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                uiAgent = IAppMonitor.Stub.asInterface(service);
                syncQueue.put(1);
            } catch (InterruptedException e) {
                // will never happen, since the queue starts with one available slot
            }
            VLog.d("CloneAgent", "connected "+ name);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            uiAgent = null;
        }
    };
}
