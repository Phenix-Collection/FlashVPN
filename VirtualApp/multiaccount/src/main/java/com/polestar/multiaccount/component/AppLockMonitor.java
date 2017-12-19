package com.polestar.multiaccount.component;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.component.activity.AppLockActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.LockPatternUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.widgets.locker.AppLockWindow;
import com.polestar.multiaccount.widgets.locker.AppLockWindowManager;

import java.util.HashMap;
import java.util.List;

/**
 * Created by guojia on 2017/1/4.
 */

public class AppLockMonitor {

    private String mUnlockedForegroudPkg;
    private static AppLockMonitor sInstance = null;
    private HashMap<String , AppModel> modelHashMap = new HashMap<>();
    private Handler mHandler;
    private static long relockDelay = PreferencesUtils.getLockInterval(); //if paused for 2 minutes, and then resume, it need be locked
    private final static int MSG_DELAY_LOCK_APP = 0;
    public final static int MSG_PACKAGE_UNLOCKED = 1;
    public final static int MSG_PRELOAD_AD = 2;
    public final static int MSG_SHOW_LOCKER = 3;
    public final static int MSG_HIDE_LOCKER = 4;
    public final static String CONFIG_APPLOCK_PRELOAD_INTERVAL = "applock_preload_interval";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";
    public final static String CONFIG_USING_ACTIVITY_LOCK = "conf_using_activity_lock";
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
                            mAdLoader.loadAd(1, null);
                            long interval = RemoteConfig.getLong(CONFIG_APPLOCK_PRELOAD_INTERVAL);
                            MLogs.d("Applocker schedule next ad at " + interval);
                            if (interval >= 15*60*000) {
                                mHandler.sendEmptyMessageDelayed(MSG_PRELOAD_AD, interval);
                            }
                        }
                    case MSG_SHOW_LOCKER:
                        MLogs.d("show locker ");
                        if (usingLockActivity()) {
                            AppLockActivity.start(VirtualCore.get().getContext(), (String) msg.obj);
                        } else {
                            AppLockWindow window = (AppLockWindow) msg.obj;
                            if (window != null) {
                                window.show(!adFree);
                            }
                        }
                        break;
                    case MSG_HIDE_LOCKER:
                        MLogs.d("dismiss locker ");
                        if (!usingLockActivity()) {
                            AppLockWindow locker = (AppLockWindow) msg.obj;
                            if (locker != null) {
                                locker.dismiss();
                            }
                        }
                        break;
                }
            }
        };
        initSetting();
    }

    private void preloadAd() {
        mHandler.removeMessages(MSG_PRELOAD_AD);
        mHandler.sendEmptyMessage(MSG_PRELOAD_AD);
    }

    public static boolean usingLockActivity() {
        return RemoteConfig.getBoolean(CONFIG_USING_ACTIVITY_LOCK);
    }

    private Object wrapMsgObj(String pkg, AppLockWindow window) {
        return usingLockActivity() ? pkg: window;
    }
    private void initSetting() {
        MLogs.d("initSetting");
        List<AppModel> list = DbManager.queryAppList(MApp.getApp());
        for (AppModel model: list) {
            modelHashMap.put(model.getPackageName(), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        adFree = false;
        LockPatternUtils.setTempKey(PreferencesUtils.getEncodedPatternPassword(MApp.getApp()));
        mAdLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, MApp.getApp());
        mAdLoader.setBannerAdSize(AppLockWindow.getBannerSize());
        preloadAd();
    }

    public FuseAdLoader getAdLoader(){
        return mAdLoader;
    }

    public void reloadSetting(String newKey, boolean adFree, long interval) {
        MLogs.d("reloadSetting adfree:" + adFree);
        modelHashMap.clear();
        DbManager.resetSession();
        List<AppModel> list = DbManager.queryAppList(MApp.getApp());
        for (AppModel model: list) {
            modelHashMap.put(model.getPackageName(), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        preloadAd();
        //if (this.adFree != adFree) {
            mAppLockWindows.removeAll();
        //}
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

    public void unlocked(String pkg) {
        mHandler.sendMessage(mHandler.obtainMessage(AppLockMonitor.MSG_PACKAGE_UNLOCKED, wrapMsgObj(pkg, null)));
    }


    public void onActivityResume(String pkg) {
        MLogs.d(TAG, "onActivityResume " + pkg);
        AppModel model = modelHashMap.get(pkg);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + pkg);
            return;
        }
        if (model.getLockerState() != AppConstants.AppLockState.DISABLED
                && hasLocker()) {
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
                mHandler.removeMessages(MSG_SHOW_LOCKER, wrapMsgObj(pkg, lockWindow));
                mHandler.removeMessages(MSG_HIDE_LOCKER,  wrapMsgObj(pkg, lockWindow));
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_LOCKER,  wrapMsgObj(pkg, appLockWindow)));
            }
        }
        //Remove the same object with send
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, model.getPackageName());
        //mUnlockedForegroudPkg = pkg;
    }

    public void onActivityPause(String pkg) {
        MLogs.d(TAG, "onActivityPause " + pkg + " delay relock: " + relockDelay);
        AppLockWindow window = mAppLockWindows.get(pkg);
        AppModel model = modelHashMap.get(pkg);
        if (window != null) {
            mHandler.removeMessages(MSG_SHOW_LOCKER,  wrapMsgObj(pkg, window));
            mHandler.removeMessages(MSG_HIDE_LOCKER, wrapMsgObj(pkg, window));
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE_LOCKER, wrapMsgObj(pkg, window)), 500);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, model.getPackageName()),
                relockDelay);
    }

    private boolean hasLocker() {
        return hasAppLocked && !TextUtils.isEmpty(LockPatternUtils.getTempKey());
    }
}
