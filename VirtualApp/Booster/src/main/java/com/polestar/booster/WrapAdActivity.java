package com.polestar.booster;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.polestar.booster.mgr.HomeListener;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by guojia on 2018/2/9.
 */

public class WrapAdActivity extends Activity {

    private final static String EXTRA_AD_SLOT = "ad_slot";
    private HomeListener mHomeListener;
    private Handler mHandler;

    public static void start(Context context, String slot) {
        BoosterLog.log("WrapAdActivity start for " + slot);
        Intent intent = new Intent(context, WrapAdActivity.class);
        intent.putExtra(EXTRA_AD_SLOT, slot);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoosterLog.log("WrapAdActivity onCreate");
        mHandler = new Handler();
        mHomeListener = new HomeListener(this.getApplication());
        mHomeListener.startListen(new HomeListener.KeyFun() {
            @Override
            public void home() {
                BoosterLog.log("home_key");
                finishAndRemoveRecent();
                mHomeListener.stopListen();
            }

            @Override
            public void recent() {
                BoosterLog.log("recent");
                finishAndRemoveRecent();
                mHomeListener.stopListen();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                    }
                }, 0);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                    }
                }, 500);

            }

            @Override
            public void longHome() {
                BoosterLog.log("longHome");
                finishAndRemoveRecent();
            }
        });

        getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                BoosterLog.log("onActivityResumed " + activity);
                if(activity.getComponentName().getClassName().contains("com.polestar.ads.AdActivity")
                        ||activity.getComponentName().getClassName().contains("com.batmobi.BatMobiActivity")
                        || activity.getComponentName().getClassName().contains(WrapAdActivity.class.getSimpleName())) {
                    return;
                } else {
                    if (mHomeListener != null) {
                        BoosterLog.log("stopListen");
                        mHomeListener.stopListen();
                        getApplication().unregisterActivityLifecycleCallbacks(this);
                    }
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                BoosterLog.log("onActivityPaused " + activity);
                if(activity.getComponentName().getClassName().contains("")
                        ||activity.getComponentName().getClassName().contains("")) {
                    if (mHomeListener != null) {
                        BoosterLog.log("stopListen");
                        mHomeListener.stopListen();
                        getApplication().unregisterActivityLifecycleCallbacks(this);
                    }
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        String slot = getIntent().getStringExtra(EXTRA_AD_SLOT);
        FuseAdLoader.get(slot, this).loadAd(this, 2, new IAdLoadListener() {

            @Override
            public void onAdLoaded(IAdAdapter ad) {
                if (ad.isInterstitialAd()) {
                    ad.show();
                }
                finishAndRemoveRecent();
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onError(String error) {
                finishAndRemoveRecent();
            }

            @Override
            public void onRewarded(IAdAdapter ad) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (Build.VERSION.SDK_INT >= 21) {
//            finishAndRemoveTask();
//        } else {
//            finish();
//        }
//    }
}
