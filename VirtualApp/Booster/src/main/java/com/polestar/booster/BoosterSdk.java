package com.polestar.booster;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.polestar.booster.util.AndroidUtil;
import com.polestar.welive.WeLive;


public class BoosterSdk {

    public static boolean DEBUG = false;

    static final String PREF_NAME = "booster_config";

    static final String PREF_KEY_BOOST_SHORTCUT_CREATED = "boost_shortcut_created";
    static final String PREF_KEY_BOOST_SHORTCUT_CREATE_COUNT = "boost_shortcut_create_count";
    static final String PREF_KEY_LAST_BOOST_SHORTCUT_REMOVE_TIME = "last_boost_shortcut_remove_time";
    static final String PREF_KEY_LAST_BOOST_SHORTCUT_CREATE_OR_UPDATE_TIME = "last_boost_shortcut_create_or_update_time";

    public static final String EXTRA_SHORTCUT_CLICK_FROM = "FROM";

    public interface IEventReporter {
        void reportEvent(String s, Bundle b);
        void reportWake(String s);
    }
    public static class BoosterConfig {
        public String boostAdSlot = "slot_boost_ad";
        public boolean isAutoClean = false;
        public boolean isAutoCreateShortcut = true;
        public boolean isInstallAd = true;
        public boolean isUnlockAd = true;
        public int autoDismissTime = 20*1000;
        public long autoCreateInterval = 3*24*60*60*1000;
        public long autoCleanInterval = 5*60*60*1000;
        public long autoAdFirstInterval = 3*24*60*60*1000;
        public long autoAdInterval = BuildConfig.DEBUG? 0: 8*60*60*1000;
        public boolean avoidShowIfHistory = true;
        public boolean showNotification = true;
        public String accountName = "Clone Messaging Daemon";
    }

    //all res id
    public static class BoosterRes {
        public int titleString;
        public int boosterShorcutIcon;
        public int innerWheelImage;
        public int outterWheelImage;
    }


    static public BoosterConfig boosterConfig = new BoosterConfig();
    static public BoosterRes boosterRes;

    static Context sContext;



    public synchronized static void init(Context context, BoosterConfig config, BoosterRes res, IEventReporter reporter) {
        BoosterLog.sReporter = reporter;
        sContext = context.getApplicationContext();
        // start init
        boosterConfig = config;
        boosterRes = res;
        WeLive.ACCOUNT_TYPE = context.getPackageName() + ".clone.daemon";
        Booster.startInit(sContext);
        BoosterLog.log("Booster init");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WeLive.startSync(context);
        }
        WeLive.startJob(context);
    }

    public static void startClean(Context context, String from) {
        Booster.startCleanShortcutClick(context.getApplicationContext(), from);
    }

    public static void checkCreateCleanShortcut() {
        if (!BoosterSdk.boosterConfig.isAutoCreateShortcut) {
            return;
        }

        long current = System.currentTimeMillis();

        SharedPreferences sp = sContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);;
        boolean cleanShortcutCreated = sp.getBoolean(PREF_KEY_BOOST_SHORTCUT_CREATED, false);
        long cleanShortcutCreateCount = sp.getLong(PREF_KEY_BOOST_SHORTCUT_CREATE_COUNT, 0L);
        long lastCleanShortcutRemoveTime = sp.getLong(PREF_KEY_LAST_BOOST_SHORTCUT_REMOVE_TIME, 0L);
        long lastCleanShortcutCreateOrUpdateTime = sp.getLong(PREF_KEY_LAST_BOOST_SHORTCUT_CREATE_OR_UPDATE_TIME, AndroidUtil.getFirstInstallTime(sContext));

        SharedPreferences.Editor ed = sp.edit();
        boolean dirty = false;

        do {
            boolean maybeRemovedDetected = cleanShortcutCreated && !AndroidUtil.hasShortcut(sContext,
                    sContext.getResources().getString(BoosterSdk.boosterRes.titleString));
            if (maybeRemovedDetected) {
                ed.putBoolean(PREF_KEY_BOOST_SHORTCUT_CREATED, false);
                ed.putLong(PREF_KEY_LAST_BOOST_SHORTCUT_REMOVE_TIME, current);
                dirty = true;
                break;
            }

            long cleanShortcutAutoCreateInterval = BoosterSdk.boosterConfig.autoCreateInterval;
            boolean needCreate = !cleanShortcutCreated && (current - lastCleanShortcutRemoveTime) > cleanShortcutAutoCreateInterval;
            long cleanShortcutUpdateInterval = BoosterSdk.boosterConfig.autoCreateInterval;
            boolean needUpdate = cleanShortcutCreated && (current - lastCleanShortcutCreateOrUpdateTime) > cleanShortcutUpdateInterval;
            if (!needCreate && !needUpdate)
                break;

            if (needUpdate)
                AndroidUtil.delShortcut(sContext, BoosterShortcutActivity.class, sContext.getResources().getString(BoosterSdk.boosterRes.titleString));
            Intent shortcutIntent = new Intent(sContext, BoosterShortcutActivity.class);
            shortcutIntent.setAction(BoosterShortcutActivity.class.getName());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            AndroidUtil.addShortcut(sContext, shortcutIntent, BoosterSdk.boosterRes.boosterShorcutIcon, sContext.getResources().getString(BoosterSdk.boosterRes.titleString));

            ed.putBoolean(PREF_KEY_BOOST_SHORTCUT_CREATED, true);
            if (needCreate) {
                ed.putLong(PREF_KEY_BOOST_SHORTCUT_CREATE_COUNT, cleanShortcutCreateCount + 1);
                ed.putLong(PREF_KEY_LAST_BOOST_SHORTCUT_CREATE_OR_UPDATE_TIME, current);
            }
            if (needUpdate) {
                ed.putLong(PREF_KEY_LAST_BOOST_SHORTCUT_CREATE_OR_UPDATE_TIME, current);
            }
            dirty = true;

        } while (false);

        if (dirty) {
            ed.apply();
        }
    }
}
