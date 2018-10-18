package com.polestar.booster.mgr;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.polestar.booster.BoosterActivity;
import com.polestar.booster.BoosterLog;
import com.polestar.booster.BoosterSdk;
import com.polestar.booster.WrapAdActivity;
import com.polestar.booster.util.AndroidUtil;
import com.polestar.booster.util.HandlerTimer;
import com.polestar.booster.util.TimeUtil;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


public class BoostMgr {

    static final String PREF_NAME = "cleaner_status";
    static final String PREF_KEY_LAST_TIME_SHOW_CLEAN = "last_time_show_clean";
    static final String PREF_KEY_DAILY_SHOW_CLEAN_DATE = "daily_show_clean_date";
    static final String PREF_KEY_DAILY_SHOW_CLEAN_COUNT = "daily_show_clean_count";
    static final String PREF_KEY_LAST_TIME_DO_CLEAN = "last_time_do_clean";


    static final String PREF_KEY_LAST_TIME_SHOW_AUTO_AD = "last_time_show_auto_ad";

    private static BoostMgr sInstance;
    private static final int AUTO_AD_HISTORY_ACTIVITY_INSTALL = 11;
    private static final int OK_INSTALL = 10;
    private static final int NO_FILL_INSTALL = 12;
    private static final int AUTO_AD_HISTORY_ACTIVITY_UNLOCK = 21;
    private static final int OK_UNLOCK = 20;
    private static final int NO_FILL_UNLOCK = 22;


    public static BoostMgr getInstance(Context context) {
        if (sInstance != null)
            return sInstance;

        synchronized (BoostMgr.class) {
            if (sInstance != null)
                return sInstance;

            sInstance = new BoostMgr(context);
            return sInstance;
        }
    }

    final Context mContext;
    final Handler mHandler = new Handler(Looper.getMainLooper());
    final WindowManager mWindowManager;

    WeakReference<Activity> mCleanerViewRef;

    public BoostMgr(Context context) {
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    private static SharedPreferences sp(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isCleanerShowing() {
        if (mCleanerViewRef == null)
            return false;

        Activity view = mCleanerViewRef.get();
        if (view == null)
            return false;

        return !view.isFinishing();
    }

    public boolean showCleaner(final boolean shortcut, final String from) {

        if (isCleanerShowing()) {
            return false;
        }

        try {
           BoosterActivity.startCleanActivity(this.mContext, shortcut, sCleanerActivityListener, from);

            int count = 0;
            if (!shortcut)
                count = onCleanShown(mContext);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private BoosterActivity.CleanActivityListener sCleanerActivityListener = new BoosterActivity.CleanActivityListener(){
        public void onActivityCreate(Activity activity) {
            mCleanerViewRef = new WeakReference(activity);
        }
    };


    public boolean dismissCleaner() {
        Activity activity = this.mCleanerViewRef != null ? (Activity)this.mCleanerViewRef.get() : null;
        if (activity == null) {
            return false;
        }
        if (!isCleanerActivityShowing()) {
            return false;
        }
        activity.finish();
        return true;
    }


    public boolean isCleanerActivityShowing() {

        if (this.mCleanerViewRef == null) {
            return false;
        }
        Activity activity = (Activity)this.mCleanerViewRef.get();
        if (activity == null) {
            return false;
        }
        if ((Build.VERSION.SDK_INT > 17) && (activity.isDestroyed())) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }
        return true;
    }

    private void resetConfigs() {
    }

    private void updateConfigs() {

    }

    boolean mBatteryShowing = false;

    HandlerTimer mPreloadAdPollTimer;

    boolean stopPreloadAdPollTimer() {
        if (mPreloadAdPollTimer == null)
            return false;

        mPreloadAdPollTimer.stop();
        mPreloadAdPollTimer = null;
        return true;
    }

    public void onScreenOff() {

        clearCheckStartCleanerJob();
        boolean dismissed = dismissCleaner();

        mBatteryShowing = false;

        boolean allowPreloadAdOnScreenOff = false;
        if (allowPreloadAdOnScreenOff)
            preloadAd("screen_off");

        if (BoosterSdk.boosterConfig.allowPreloadAdTimer) {
            final long preloadAdInterval = BoosterSdk.boosterConfig.preloadAdTimerInterval;
            if (preloadAdInterval > 0) {
                mPreloadAdPollTimer = new HandlerTimer(mHandler, new HandlerTimer.Task() {
                    @Override
                    public boolean run() {
                        preloadAd("poll");
                        return false;
                    }
                }, preloadAdInterval);
                mPreloadAdPollTimer.start(preloadAdInterval);
            }
        }
    }

    public void onScreenOn() {
        //updateConfigs();
        if (BoosterSdk.boosterConfig.isPreloadOnUnlock)
            preloadAd("screen_on");
    }

    public void onUserPresent() {

        // stop poll preload ad

        if (!BoosterSdk.boosterConfig.isUnlockAd) {
            BoosterLog.log("unlock not enabled");
            return;
        }

        if (checkShowUnlockAd()) {
            //BoosterLog.log("unlock not good time");
            doShowUnlockAd();
            return;
        }
    }

    private void doInstallAd() {
        FuseAdLoader.get(BoosterSdk.boosterConfig.installAdSlot,mContext).loadAd(2, new IAdLoadListener() {

            @Override
            public void onAdLoaded(IAdAdapter ad) {
                BoosterLog.autoAdShow(OK_INSTALL);
                WrapAdActivity.start(mContext, BoosterSdk.boosterConfig.installAdSlot);
                updateAutoAdShowTime(mContext);
            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onError(String error) {
                BoosterLog.autoAdShow(NO_FILL_INSTALL);
            }
        });
    }

    public static Activity getHistoryActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }catch (Throwable ex) {
            return  null;
        }

        return null;
    }

    public static Activity getActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);

            Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
            if (activities == null)
                return null;

            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        }catch (Throwable ex) {
            return  null;
        }

        return null;
    }
    private void finishAndRemoveRecent() {
        Activity activity = getActivity();
        BoosterLog.log("finishAndRemoveRecent");
        if (activity != null) {
            BoosterLog.log("activity " + activity.getComponentName());
            if (Build.VERSION.SDK_INT >= 21) {
                activity.finishAndRemoveTask();
            } else {
                activity.finish();
            }
        }

//        if (Build.VERSION.SDK_INT >= 21) {
//            finishAndRemoveTask();
//        } else {
//            finish();
//        }
    }

    private void doShowUnlockAd(){
        FuseAdLoader.get(BoosterSdk.boosterConfig.unlockAdSlot,mContext).loadAd(2, 3000, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                BoosterLog.log("Unlock ad loaded");
                //startMonitor();
                //ad.show();
                BoosterLog.autoAdShow(OK_UNLOCK);
                WrapAdActivity.start(mContext, BoosterSdk.boosterConfig.unlockAdSlot);
                updateAutoAdShowTime(mContext);
            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onError(String error) {
                BoosterLog.autoAdShow(NO_FILL_UNLOCK);
            }
        });
    }

    public void onShowBattery() {

        mBatteryShowing = true;
        clearCheckStartCleanerJob();
    }

    public void onDismissBattery() {

        mBatteryShowing = false;
        postCheckStartCleanerJob(TimeUtil.SECOND * 1L);
    }

    public void onBatteryKillLockscreen() {

        mBatteryShowing = true;
        clearCheckStartCleanerJob();
    }

    public void onCleanShortcutClick(String from) {

        showCleaner(true, from);
    }

    private boolean checkPreload(final String chance) {

        return check(mContext, new CheckerCallback() {
            @Override
            public boolean onFailFunctionClosed() {
                return false;
            }

            @Override
            public boolean onFailAutoCleanClosed(boolean autoCleanEnabled) {
                return false;
            }

            @Override
            public boolean onFailNoConfig() {
                return false;
            }

            @Override
            public boolean onFailCountLimit(int dailyCleanCountLimit, int dailyCleanCount) {
                return false;
            }

            @Override
            public boolean onFailLocationDisabled() {
                return false;
            }

            @Override
            public boolean onFailNoNetwork() {
                return false;
            }
        });
    }

    private void preloadAd(final String chance) {
    }

    final Runnable mCheckStartCleanerJob = new Runnable() {
        @Override
        public void run() {
            checkStartCleaner();
        }
    };

    private void clearCheckStartCleanerJob() {
        mHandler.removeCallbacks(mCheckStartCleanerJob);
    }

    private void postCheckStartCleanerJob(long delay) {
        mHandler.removeCallbacks(mCheckStartCleanerJob);
        mHandler.postDelayed(mCheckStartCleanerJob, delay);
    }

    public interface CheckerCallback {
        boolean onFailFunctionClosed();

        boolean onFailAutoCleanClosed(boolean autoCleanEnabled);

        boolean onFailNoConfig();

        boolean onFailCountLimit( int dailyCleanCountLimit, int dailyCleanCount);

        boolean onFailLocationDisabled();

        boolean onFailNoNetwork();
    }

    public static boolean check(final Context context, final CheckerCallback callback) {

        boolean autoCleanEnabled = BoosterSdk.boosterConfig.isAutoClean;
        if (!autoCleanEnabled ) {
            if (callback != null)
                return callback.onFailAutoCleanClosed( autoCleanEnabled);
            return false;
        }

        int dailyCleanCount = getDailyShowCleanCount(context);
        if (System.currentTimeMillis() - getLastTimeShowClean(context) < BoosterSdk.boosterConfig.autoCleanInterval ) {
            return false;
        }

//        if (CommonSdk.isBlocked(context)) {
//            if (callback != null)
//                return callback.onFailLocationDisabled(config, configInfo);
//            return false;
//        }

        if (!AndroidUtil.isNetworkAvailable(context)) {
            if (callback != null)
                return callback.onFailNoNetwork();
            return false;
        }

        return true;
    }

    public boolean checkStartCleaner() {

        KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();
        if (locked) {
            BoosterSdk.useRealUserPresent(true);
            return false;
        }

        if (mBatteryShowing) {
            return false;
        }

        int callState = AndroidUtil.getCallState(mContext);
        if (callState != TelephonyManager.CALL_STATE_IDLE) {
            return false;
        }

        if (!check(mContext, new CheckerCallback() {
            @Override
            public boolean onFailFunctionClosed() {
                return false;
            }

            @Override
            public boolean onFailAutoCleanClosed( boolean autoCleanEnabled) {
                return false;
            }

            @Override
            public boolean onFailNoConfig() {
                return false;
            }

            @Override
            public boolean onFailCountLimit(int dailyCleanCountLimit, int dailyCleanCount) {
                return false;
            }

            @Override
            public boolean onFailLocationDisabled() {
                return false;
            }

            @Override
            public boolean onFailNoNetwork() {
                return false;
            }
        }))
            return false;

        long cleanTimeInterval = BoosterSdk.boosterConfig.autoCleanInterval;
        long current = System.currentTimeMillis();
        long lastTimeShowClean = getLastTimeShowClean(mContext);
        if ((current - lastTimeShowClean) < cleanTimeInterval) {
            return false;
        }

        long totalMemory = AndroidUtil.getTotalMemory(mContext);
        long availMemory = AndroidUtil.getAvailMemory(mContext);
        long usedMemory = totalMemory - availMemory;
        int usedPercentage = (int) (usedMemory * 100 / totalMemory);
        int usedPercentageThreshold = BoosterSdk.boosterConfig.memoryThreshold;
        if (usedPercentage < usedPercentageThreshold) {
            return false;
        }

        //TODO JJJ
//        if (!ConfigUtil.allowDisplayAutoCleanWithoutAdCached(configInfo) && !AdAgent.getInstance().isHavaADCache(config.getSlotId())) {
//            if (log.isDebugEnabled())
//                log.debug("checkStartCleaner false" + " adCached:" + false);
//            return false;
//        }

        return showCleaner(false, "auto");
    }

    public static long getLastTimeDoClean(Context context) {
        return sp(context).getLong(PREF_KEY_LAST_TIME_DO_CLEAN, 0L);
    }

    public static void setLastTimeDoClean(Context context, long current) {
        sp(context).edit().putLong(PREF_KEY_LAST_TIME_DO_CLEAN, current).apply();
    }

    private static long getLastTimeShowClean(Context context) {
        return sp(context).getLong(PREF_KEY_LAST_TIME_SHOW_CLEAN, 0L);
    }

    private static int getDailyShowCleanCount(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_CLEAN_DATE, null);
        boolean sameDay = date.equals(lastDate);
        if (!sameDay)
            return 0;

        return sp.getInt(PREF_KEY_DAILY_SHOW_CLEAN_COUNT, 0);
    }

    private static int onCleanShown(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_CLEAN_DATE, null);
        boolean sameDay = date.equals(lastDate);

        final int count;
        SharedPreferences.Editor e = sp.edit();
        e.putLong(PREF_KEY_LAST_TIME_SHOW_CLEAN, System.currentTimeMillis());
        if (sameDay) {
            int lastCount = sp.getInt(PREF_KEY_DAILY_SHOW_CLEAN_COUNT, 0);
            count = lastCount + 1;
            e.putInt(PREF_KEY_DAILY_SHOW_CLEAN_COUNT, lastCount + 1);
        } else {
            count = 1;
            e.putString(PREF_KEY_DAILY_SHOW_CLEAN_DATE, date);
            e.putInt(PREF_KEY_DAILY_SHOW_CLEAN_COUNT, 1);
        }
        e.apply();

        return count;
    }


    public static long getLastTimeShowAutoAd(Context context) {
        long ret = sp(context).getLong(PREF_KEY_LAST_TIME_SHOW_AUTO_AD, 0);
        if (ret == 0) {
            //not showed, cold down for one interval
            ret = System.currentTimeMillis();
            sp(context).edit().putLong(PREF_KEY_LAST_TIME_SHOW_AUTO_AD, ret).apply();
        }
        return ret;
    }

    private static void updateAutoAdShowTime(Context context) {
        SharedPreferences sp = sp(context);
        SharedPreferences.Editor e = sp.edit();
        e.putLong(PREF_KEY_LAST_TIME_SHOW_AUTO_AD, System.currentTimeMillis());
        e.apply();
    }

    public boolean checkShowUnlockAd() {

        Context context = mContext;

        if (!BoosterSdk.boosterConfig.isUnlockAd) {
            return false;
        }

        long unlockAdFirstTimeInterval = BoosterSdk.boosterConfig.autoAdFirstInterval;
        long firstTimeInstall = AndroidUtil.getFirstInstallTime(context);
        long current = System.currentTimeMillis();
        if ((current - firstTimeInstall) < unlockAdFirstTimeInterval) {
            BoosterLog.log("current " + current + " firstInstall: " + firstTimeInstall + " autoAdFirstInterval " + BoosterSdk.boosterConfig.autoAdFirstInterval);
            return false;
        }

        long unLockAdTimeInterval = BoosterSdk.boosterConfig.autoAdInterval;
        long lastTimeShowUnlockAd = getLastTimeShowAutoAd(context);
        if ((current - lastTimeShowUnlockAd) < unLockAdTimeInterval) {
            BoosterLog.log("current " + current + " lastTimeShowUnlockAd: " + lastTimeShowUnlockAd + " unLockAdTimeInterval " + unLockAdTimeInterval);
            return false;
        }

        if (getHistoryActivity() != null) {
            BoosterLog.log("current has acitivity: " + getHistoryActivity());
            BoosterLog.autoAdShow(AUTO_AD_HISTORY_ACTIVITY_UNLOCK);
            return false;
        }
        return true;
    }


    public void onInstall(String packageName) {
        BoosterLog.log("onInstall " + packageName);
        checkShowInstallAd(packageName);
    }

    private void checkShowInstallAd(String packageName) {
        final Context context = mContext;

        if (!BoosterSdk.boosterConfig.isInstallAd) {
            BoosterLog.log("Not allow install ad");
            return;
        }


        long installAdFirstTimeInterval = BoosterSdk.boosterConfig.autoAdFirstInterval;
        long firstTimeInstall = AndroidUtil.getFirstInstallTime(context);
        long current = System.currentTimeMillis();
        if ((current - firstTimeInstall) < installAdFirstTimeInterval) {
            BoosterLog.log("In cold time current:" + current + " firstTimeInstall:"+firstTimeInstall + " interv:"+installAdFirstTimeInterval);
            return;
        }

        long installAdTimeInterval = BoosterSdk.boosterConfig.autoAdInterval;
        long lastTimeShowUnlockAd = getLastTimeShowAutoAd(context);
        if ((current - lastTimeShowUnlockAd) < installAdTimeInterval) {
            BoosterLog.log("Dont show too much");
            return ;
        }

        if (getHistoryActivity() != null) {
            BoosterLog.autoAdShow(AUTO_AD_HISTORY_ACTIVITY_INSTALL);
            return;
        }
        doInstallAd();
    }
}
