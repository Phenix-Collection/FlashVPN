package com.google.android.gms.booster.mgr;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.google.android.gms.booster.BoosterActivity;
import com.google.android.gms.booster.BoosterSdk;
import com.google.android.gms.booster.util.AndroidUtil;
import com.google.android.gms.booster.util.HandlerTimer;
import com.google.android.gms.booster.util.TimeUtil;
import java.lang.ref.WeakReference;



public class BoostMgr {

    static final String PREF_NAME = "cleaner_status";
    static final String PREF_KEY_LAST_TIME_SHOW_CLEAN = "last_time_show_clean";
    static final String PREF_KEY_DAILY_SHOW_CLEAN_DATE = "daily_show_clean_date";
    static final String PREF_KEY_DAILY_SHOW_CLEAN_COUNT = "daily_show_clean_count";
    static final String PREF_KEY_LAST_TIME_DO_CLEAN = "last_time_do_clean";


    static final String PREF_KEY_LAST_TIME_SHOW_UNLOCK_AD = "last_time_show_unlock_ad";
    static final String PREF_KEY_DAILY_SHOW_UNLOCK_AD_DATE = "daily_show_unlock_ad_date";
    static final String PREF_KEY_DAILY_SHOW_UNLOCK_AD_COUNT = "daily_show_unlock_ad_count";
    static final String PREF_KEY_LAST_TIME_LOAD_UNLOCK_AD = "last_time_load_unlock_ad";


    static final String PREF_KEY_DAILY_SHOW_INSTALL_AD_COUNT = "daily_show_install_ad_count";
    static final String PREF_KEY_DAILY_SHOW_INSTALL_AD_DATE = "daily_show_install_ad_date";

    static final String UNLOCK_AD = "unlock_ad";
    static final String INSTALL_AD = "Install_ad";

    static final long LOAD_UNLOCK_AD_INTERVAL = 1000L;


    private static BoostMgr sInstance;

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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
            return;
        }

        if (checkShowUnlockAd()) {
            return;
        }

        clearCheckStartCleanerJob();
        if (AndroidUtil.isBatteryPlugged(mContext)) {
            postCheckStartCleanerJob(TimeUtil.SECOND * 1L);
        } else {
            postCheckStartCleanerJob(0);
        }
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

        //TODO JJJJ
//        if (AdAgent.getInstance().isHavaADCache(config.getSlotId())) {
//            if (log.isDebugEnabled())
//                log.debug("preloadAd ad cached" + " chance:" + chance);
//            return;
//        }

        if (!checkPreload(chance))
            return;

        final long begin = System.currentTimeMillis();

        //TODO JJJJ
//        Ad ad = new Ad.Builder(mContext, config.getSlotId()).isPreLoad(true).build();
//        AdAgent.getInstance().loadAd(mContext, ad, new OnAdLoadListener() {
//            @Override
//            public void onLoad(IAd iAd) {
//                if (log.isDebugEnabled())
//                    log.debug("preloadAd onLoad" + " chance:" + chance + " used:" + (System.currentTimeMillis() - begin) + "ms" + " cached:" + AdAgent.getInstance().isHavaADCache(config.getSlotId()));
//            }
//
//            @Override
//            public void onLoadFailed(AdError adError) {
//                if (log.isDebugEnabled())
//                    log.debug("preloadAd onLoadFailed" + " chance:" + chance + " used:" + (System.currentTimeMillis() - begin) + "ms");
//            }
//
//            @Override
//            public void onLoadInterstitialAd(WrapInterstitialAd wrapInterstitialAd) {
//                if (log.isDebugEnabled())
//                    log.debug("preloadAd onLoadInterstitialAd" + " chance:" + chance + " used:" + (System.currentTimeMillis() - begin) + "ms");
//            }
//        });
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


    public static long getLastTimeShowUnlockAd(Context context) {
        return sp(context).getLong(PREF_KEY_LAST_TIME_SHOW_UNLOCK_AD, 0);
    }

    private static int onUnlockAdShow(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_UNLOCK_AD_DATE, null);
        boolean sameDay = date.equals(lastDate);

        final int count;
        SharedPreferences.Editor e = sp.edit();
        e.putLong(PREF_KEY_LAST_TIME_SHOW_UNLOCK_AD, System.currentTimeMillis());
        if (sameDay) {
            int lastCount = sp.getInt(PREF_KEY_DAILY_SHOW_UNLOCK_AD_COUNT, 0);
            count = lastCount + 1;
            e.putInt(PREF_KEY_DAILY_SHOW_UNLOCK_AD_COUNT, lastCount + 1);
        } else {
            count = 1;
            e.putString(PREF_KEY_DAILY_SHOW_UNLOCK_AD_DATE, date);
            e.putInt(PREF_KEY_DAILY_SHOW_UNLOCK_AD_COUNT, 1);
        }
        e.apply();

        return count;
    }

    private static int getDailyShowUnlockAdCount(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_UNLOCK_AD_DATE, null);
        boolean sameDay = date.equals(lastDate);
        if (!sameDay)
            return 0;

        return sp.getInt(PREF_KEY_DAILY_SHOW_UNLOCK_AD_COUNT, 0);
    }

    private static int onInstallAdShow(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_INSTALL_AD_DATE, null);
        boolean sameDay = date.equals(lastDate);

        final int count;
        SharedPreferences.Editor e = sp.edit();
        if (sameDay) {
            int lastCount = sp.getInt(PREF_KEY_DAILY_SHOW_INSTALL_AD_COUNT, 0);
            count = lastCount + 1;
            e.putInt(PREF_KEY_DAILY_SHOW_INSTALL_AD_COUNT, lastCount + 1);
        } else {
            count = 1;
            e.putString(PREF_KEY_DAILY_SHOW_INSTALL_AD_DATE, date);
            e.putInt(PREF_KEY_DAILY_SHOW_INSTALL_AD_COUNT, 1);
        }
        e.apply();

        return count;
    }

    private static int getDailyShowInstallAdCount(Context context) {
        String date = TimeUtil.dateNow();
        SharedPreferences sp = sp(context);
        String lastDate = sp.getString(PREF_KEY_DAILY_SHOW_INSTALL_AD_DATE, null);
        boolean sameDay = date.equals(lastDate);
        if (!sameDay)
            return 0;

        return sp.getInt(PREF_KEY_DAILY_SHOW_INSTALL_AD_COUNT, 0);
    }

    public boolean checkShowUnlockAd() {

        Context context = mContext;

        if (!BoosterSdk.boosterConfig.isInstallAd) {
            return false;
        }

        long unlockAdFirstTimeInterval = BoosterSdk.boosterConfig.unlockAdFirstInterval;
        long firstTimeInstall = AndroidUtil.getFirstInstallTime(context);
        long current = System.currentTimeMillis();
        if ((current - firstTimeInstall) < unlockAdFirstTimeInterval) {
            return false;
        }

        long unLockAdTimeInterval = BoosterSdk.boosterConfig.unlockAdInterval;
        long lastTimeShowUnlockAd = getLastTimeShowUnlockAd(context);
        if ((current - lastTimeShowUnlockAd) < unLockAdTimeInterval) {
            return false;
        }
        loadInterstitialAd(UNLOCK_AD, BoosterSdk.boosterConfig.unlockAdSlot, context);
        setLastTimeLoadUnlockAd(context, current);
        return true;
    }


    public void onInstall() {

        checkShowInstallAd();
    }

    private void checkShowInstallAd() {
        final Context context = mContext;

        if (!BoosterSdk.boosterConfig.isInstallAd) {
            return;
        }

        int dailyInstallAdCountLimit = BoosterSdk.boosterConfig.installAdLimit;
        int dailyInstallAdCount = getDailyShowInstallAdCount(context);
        if (dailyInstallAdCount >= dailyInstallAdCountLimit) {
            return;
        }
        loadInterstitialAd(INSTALL_AD, BoosterSdk.boosterConfig.installAdSlot, context);
    }

    private void loadInterstitialAd(final String str, final String slotId,  final Context context) {


        final long begin = System.currentTimeMillis();
        // TODO JJJJ
//        Ad ad = new Ad.Builder(context, slotId).build();
//        AdAgent.getInstance().loadAd(context, ad, new OnAdLoadListener() {
//            @Override
//            public void onLoad(IAd iAd) {
//                if (log.isDebugEnabled())
//                    log.debug("loadAd onLoad" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//            }
//
//            @Override
//            public void onLoadFailed(AdError adError) {
//                if (log.isDebugEnabled())
//                    log.debug("loadAd onLoadFailed" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//            }
//
//            @Override
//            public void onLoadInterstitialAd(WrapInterstitialAd wrapInterstitialAd) {
//                if (log.isDebugEnabled())
//                    log.debug("loadAd onLoadInterstitialAd" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//
////                long used = System.currentTimeMillis() - begin;
////                long presetTime = 3000L;
////                boolean isOverTime = used > presetTime ? true : false;
////                if (isOverTime) {
////                    log.debug("loadAd onLoadInterstitialAdOvertime" + " used:" + (System.currentTimeMillis() - begin) + "ms" + " presetTime:" + presetTime + "ms");
////                    return;
////                }
//
//                if (str.equals(UNLOCK_AD)) {
//                    onUnlockAdShow(context);
//                } else {
//                    onInstallAdShow(context);
//                }
//                wrapInterstitialAd.show();
//            }
//        });
    }

    public static void setLastTimeLoadUnlockAd(Context context, long current) {
        sp(context).edit().putLong(PREF_KEY_LAST_TIME_LOAD_UNLOCK_AD, current).apply();
    }

    private static long getLastTimeLoadUnlockAd(Context context) {
        return sp(context).getLong(PREF_KEY_LAST_TIME_LOAD_UNLOCK_AD, 0L);
    }

    private boolean isLoadUnlockAd() {
        long current = System.currentTimeMillis();
        long lastTimeLoadUnlockAd = getLastTimeLoadUnlockAd(mContext);
        if ((current - lastTimeLoadUnlockAd) < LOAD_UNLOCK_AD_INTERVAL) {
            return true;
        }
        return false;
    }
}
