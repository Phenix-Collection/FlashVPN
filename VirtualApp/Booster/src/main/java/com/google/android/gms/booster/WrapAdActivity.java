package com.google.android.gms.booster;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.booster.mgr.HomeListener;
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

    public static void start(Context context, String slot) {
        BoosterLog.log("WrapAdActivity start for " + slot);
        Intent intent = new Intent(context, WrapAdActivity.class);
        intent.putExtra(EXTRA_AD_SLOT, slot);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoosterLog.log("WrapAdActivity onCreate");
        mHomeListener = new HomeListener(this);
        mHomeListener.startListen(new HomeListener.KeyFun() {
            @Override
            public void home() {
                BoosterLog.log("home_key");
                Activity activity = getActivity();
                if (activity != null) {
                    BoosterLog.log("activity " + activity.getComponentName());
                    if (Build.VERSION.SDK_INT >= 21) {
                        activity.finishAndRemoveTask();
                        finishAndRemoveTask();
                    } else {
                        activity.finish();
                        finish();
                    }
                }
            }

            @Override
            public void recent() {
                BoosterLog.log("recent");
                Activity activity = getActivity();
                if (activity != null) {
                    BoosterLog.log("activity " + activity.getComponentName());
                    if (Build.VERSION.SDK_INT >= 21) {
                        activity.finishAndRemoveTask();
                        finishAndRemoveTask();
                    } else {
                        activity.finish();
                        finish();
                    }
                }
            }

            @Override
            public void longHome() {
                BoosterLog.log("longHome");
                Activity activity = getActivity();
                if (activity != null) {
                    BoosterLog.log("activity " + activity.getComponentName());
                    if (Build.VERSION.SDK_INT >= 21) {
                        activity.finishAndRemoveTask();
                        finishAndRemoveTask();
                    } else {
                        activity.finish();
                        finish();
                    }
                }
            }
        });
        String slot = getIntent().getStringExtra(EXTRA_AD_SLOT);
        FuseAdLoader.get(slot, this).loadAd(2, new IAdLoadListener() {
            @Override
            protected void onAdLoaded(IAdAdapter ad) {
                if (ad.isInterstitialAd()) {
                    ad.show();
                }
            }

            @Override
            protected void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            protected void onError(String error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHomeListener != null) {
            mHomeListener.stopListen();
        }
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
}