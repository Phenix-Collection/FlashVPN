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

    public void onCleanShortcutClick(String from) {

        showCleaner(true, from);
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
}
