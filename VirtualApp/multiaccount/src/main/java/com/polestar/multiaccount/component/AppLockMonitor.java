package com.polestar.multiaccount.component;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;
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
    private final static long RELOCK_DELAY = 3*1000; //if paused for 2 minutes, and then resume, it need be locked
    private final static int MSG_DELAY_LOCK_APP = 0;
    public final static int MSG_PACKAGE_UNLOCKED = 1;
    private final static String TAG = "AppLockMonitor";

    private AppLockWindowManager mAppLockWindows = AppLockWindowManager.getInstance();

    private AppLockMonitor() {
        initSetting();
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
                }
            }
        };
    }

    private void initSetting() {
        MLogs.d("initSetting");
        List<AppModel> list = DbManager.queryAppList(MApp.getApp());
        for (AppModel model: list) {
            modelHashMap.put(model.getPackageName(), model);
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
        AppModel model = modelHashMap.get(pkg);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + pkg);
            return;
        }
        if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
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
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MLogs.d(TAG, "To show lock window");
                        lockWindow.show();
                    }
                }, 0);

            }
        }
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, pkg);
        //mUnlockedForegroudPkg = pkg;
    }

    public void onActivityPause(String pkg) {
        MLogs.d(TAG, "onActivityPause " + pkg);
        AppLockWindow window = mAppLockWindows.get(pkg);
        if (window != null){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    window.dismiss();
                }
            }, 300);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, pkg),
                RELOCK_DELAY);
    }
}
