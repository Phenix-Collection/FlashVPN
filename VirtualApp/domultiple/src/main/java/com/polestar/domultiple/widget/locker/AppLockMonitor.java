package com.polestar.domultiple.widget.locker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Created by PolestarApp on 2017/1/4.
 */

public class AppLockMonitor {

    private String mUnlockedForegroudPkg;
    private static AppLockMonitor sInstance = null;
    private HashMap<String , CloneModel> modelHashMap = new HashMap<>();
    private Handler mHandler;
    private static long relockDelay = PreferencesUtils.getLockInterval(); //if paused for 2 minutes, and then resume, it need be locked
    private final static int MSG_DELAY_LOCK_APP = 0;
    public final static int MSG_PACKAGE_UNLOCKED = 1;
    public final static int MSG_PRELOAD_AD = 2;
    public final static int MSG_SHOW_LOCKER = 3;
    public final static int MSG_HIDE_LOCKER = 4;
    public final static String CONFIG_APPLOCK_PRELOAD_INTERVAL = "applock_preload_interval";
    private final static String TAG = "AppLockMonitor";

    private FuseAdLoader mAdLoader;
    private boolean hasAppLocked;
    private boolean adFree;

    private AppLockWindowManager mAppLockWindows = AppLockWindowManager.getInstance();

    private AppLockMonitor() {
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DELAY_LOCK_APP:
                        MLogs.d(TAG, "Package change to background, last foreground: " + mUnlockedForegroudPkg);
                        mUnlockedForegroudPkg = null;
                        break;
                    case MSG_PACKAGE_UNLOCKED:
                        String pkg = (String) msg.obj;
                        MLogs.d(TAG, "Package was unlocked" + pkg);
                        mUnlockedForegroudPkg = pkg;
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
                    case MSG_SHOW_LOCKER:
                        AppLockWindow window = (AppLockWindow) msg.obj;
                        if (window != null) {
                            window.show(!adFree);
                        }
                        break;
                    case MSG_HIDE_LOCKER:
                        AppLockWindow locker = (AppLockWindow) msg.obj;
                        if (locker != null) {
                            locker.dismiss();
                        }
                        break;
                }
            }
        };
        initSetting();
    }

    private boolean hasLocker() {
        return hasAppLocked && !TextUtils.isEmpty(LockPatternUtils.getTempKey());
    }
    private void preloadAd() {
        mHandler.removeMessages(MSG_PRELOAD_AD);
        mHandler.sendEmptyMessage(MSG_PRELOAD_AD);
    }

    private void initSetting() {
        MLogs.d("initSetting");
        List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
        for (CloneModel model: list) {
            modelHashMap.put(model.getPackageName(), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        adFree = false;
        mAdLoader = FuseAdLoader.get(AppLockWindow.CONFIG_SLOT_APP_LOCK, PolestarApp.getApp());
        mAdLoader.setBannerAdSize(AppLockWindow.getBannerSize());
        LockPatternUtils.setTempKey(PreferencesUtils.getEncodedPatternPassword(PolestarApp.getApp()));
        preloadAd();
    }

    public FuseAdLoader getAdLoader(){
        return mAdLoader;
    }

    public void reloadSetting(String newKey, boolean adFree, long interval) {
        MLogs.d(TAG, "reloadSetting adfree:" + adFree + " tmpkey: " + newKey);
        modelHashMap.clear();
        DBManager.resetSession();
        List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
        for (CloneModel model: list) {
            modelHashMap.put(model.getPackageName(), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                MLogs.d(TAG, "hasAppLocked " + model.getPackageName());
                hasAppLocked = true;
            }
        }
        preloadAd();
        if (this.adFree != adFree) {
            mAppLockWindows.removeAll();
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


    public void onActivityResume(String pkg) {
        MLogs.d(TAG, "onActivityResume " + pkg);
        CloneModel model = modelHashMap.get(pkg);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + pkg);
            return;
        }
        if (hasLocker() && model.getLockerState() != AppConstants.AppLockState.DISABLED) {
            MLogs.d(TAG, "Need lock app " + pkg);
            if (mUnlockedForegroudPkg == null || (!mUnlockedForegroudPkg.equals(pkg))) {
                //do lock
                MLogs.d(TAG, "Do lock app " + pkg);
                AppLockWindow appLockWindow = mAppLockWindows.get(pkg);
                if (appLockWindow == null) {
                    appLockWindow = new AppLockWindow(pkg, mHandler);
                    mAppLockWindows.add(pkg,appLockWindow);
                }
                final AppLockWindow lockWindow = appLockWindow;
                MLogs.d(TAG, "Do lock app 2" + pkg);
                mHandler.removeMessages(MSG_SHOW_LOCKER, lockWindow);
                mHandler.removeMessages(MSG_HIDE_LOCKER, lockWindow);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_LOCKER, lockWindow));
            }
        }
        //Remove the same object with send
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, model.getPackageName());
        //mUnlockedForegroudPkg = pkg;
    }

    public void onActivityPause(String pkg) {
        MLogs.d(TAG, "onActivityPause " + pkg + " delay relock: " + relockDelay);
        AppLockWindow window = mAppLockWindows.get(pkg);
        CloneModel model = modelHashMap.get(pkg);
        if (window != null) {
            mHandler.removeMessages(MSG_SHOW_LOCKER, window);
            mHandler.removeMessages(MSG_HIDE_LOCKER, window);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE_LOCKER, window), 500);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, model.getPackageName()),
                relockDelay);
    }
}
