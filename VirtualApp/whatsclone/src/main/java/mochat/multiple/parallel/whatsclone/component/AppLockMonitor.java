package mochat.multiple.parallel.whatsclone.component;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.adapters.FuseAdLoader;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.component.activity.AppLockActivity;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.db.DbManager;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import mochat.multiple.parallel.whatsclone.utils.AppManager;
import mochat.multiple.parallel.whatsclone.utils.LockPatternUtils;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;

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

    private AppLockMonitor() {
        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DELAY_LOCK_APP:
                        MLogs.d(TAG, "Package change to background, last foreground: " + mUnlockedForegroudPkg);
                        mUnlockedForegroudPkg = null;
                        break;
                    case MSG_PACKAGE_UNLOCKED: {
                            String key = (String) msg.obj;
                            MLogs.d(TAG, "Package was unlocked" + key);
                            mUnlockedForegroudPkg = key;
                        }
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
                        String key = (String) msg.obj;
                        AppLockActivity.start(VirtualCore.get().getContext(), AppManager.getNameFromKey(key),
                                AppManager.getUserIdFromKey(key));
                        break;
                    case MSG_HIDE_LOCKER:
                        MLogs.d("dismiss locker ");
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

    private void initSetting() {
        MLogs.d("initSetting");
        List<AppModel> list = DbManager.queryAppList(MApp.getApp());
        for (AppModel model: list) {
            modelHashMap.put(AppManager.getMapKey(model.getPackageName(), model.getPkgUserId()), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        adFree = false;
        LockPatternUtils.setTempKey(PreferencesUtils.getEncodedPatternPassword(MApp.getApp()));
        mAdLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, MApp.getApp());
        mAdLoader.setBannerAdSize(AppLockActivity.getBannerSize());
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
            modelHashMap.put(AppManager.getMapKey(model.getPackageName(), model.getPkgUserId()), model);
            if (model.getLockerState() != AppConstants.AppLockState.DISABLED) {
                hasAppLocked = true;
            }
        }
        preloadAd();
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
        String key = AppManager.getMapKey(pkg, userId);
        mHandler.sendMessage(mHandler.obtainMessage(AppLockMonitor.MSG_PACKAGE_UNLOCKED, key));
    }


    public void onActivityResume(String pkg, int userId) {
        String key = AppManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityResume " + key);
        AppModel model = modelHashMap.get(key);
        if (model == null || pkg == null) {
            MLogs.logBug(TAG, "cannot find cloned model : " + key);
            return;
        }
        if (model.getLockerState() != AppConstants.AppLockState.DISABLED
                && hasLocker()) {
            MLogs.d(TAG, "Need lock app " + key);
            if (mUnlockedForegroudPkg == null || (!mUnlockedForegroudPkg.equals(key))) {
                //do lock
                MLogs.d(TAG, "Do lock app " + key);
                MLogs.d(TAG, "Do lock app 2" + key);
                mHandler.removeMessages(MSG_SHOW_LOCKER, key);
                mHandler.removeMessages(MSG_HIDE_LOCKER,  key);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_LOCKER,  key));
            }
        }
        //Remove the same object with send
        mHandler.removeMessages(MSG_DELAY_LOCK_APP, key);
        //mUnlockedForegroudPkg = pkg;
    }

    public void onActivityPause(String pkg, int userId) {
        String key = AppManager.getMapKey(pkg, userId);
        MLogs.d(TAG, "onActivityPause " + key + " delay relock: " + relockDelay);
        AppModel model = modelHashMap.get(key);
        if (model != null) {
            mHandler.removeMessages(MSG_SHOW_LOCKER,  key);
            mHandler.removeMessages(MSG_HIDE_LOCKER, key);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE_LOCKER, key), 500);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_DELAY_LOCK_APP, key),
                relockDelay);
    }

    private boolean hasLocker() {
        return hasAppLocked && !TextUtils.isEmpty(LockPatternUtils.getTempKey());
    }
}
