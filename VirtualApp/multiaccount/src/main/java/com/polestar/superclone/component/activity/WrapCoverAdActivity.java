package com.polestar.superclone.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.MApp;
import com.polestar.superclone.component.AppMonitorService;
import com.polestar.superclone.utils.AppManager;
import com.polestar.superclone.utils.MLogs;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Created by guojia on 2018/6/3.
 */

public class WrapCoverAdActivity extends Activity {

    private final static String EXTRA_AD_SLOT = "ad_slot";
    private final static String EXTRA_PACKAGE = "start_pkg";
    private final static String EXTRA_USERID = "start_userId";
    private final static String TAG = "AppMonitor";
    public static void start(Context context, String slot, String pkg, int userId) {
        Intent intent = new Intent(context, WrapCoverAdActivity.class);
        intent.putExtra(EXTRA_AD_SLOT, slot);
        intent.putExtra(EXTRA_PACKAGE, pkg);
        intent.putExtra(EXTRA_USERID, userId);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP|FLAG_ACTIVITY_NO_HISTORY
                |FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String slot = getIntent().getStringExtra(EXTRA_AD_SLOT);
        String pkg = getIntent().getStringExtra(EXTRA_PACKAGE);
        int userId = getIntent().getIntExtra(EXTRA_USERID, 0);
        if (Build.VERSION.SDK_INT >= 21) {
            MApp.getApp().setCurrentAdClone(pkg, userId);
//            CustomizeAppData appData = CustomizeAppData.loadFromPref(pkg, userId);
//            setTaskDescription(new ActivityManager.TaskDescription(appData.label, appData.getCustomIcon()));
        }
        FuseAdLoader.get(slot, this).loadAd(this, 1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                try {
                    ad.show();
                    FuseAdLoader.get(slot, WrapCoverAdActivity.this).preloadAd(WrapCoverAdActivity.this);
                }catch (Throwable ex){

                }
            }

            @Override
            public void onRewarded(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {
                MLogs.d(TAG, " onCoverAdClosed");
                if(!CloneAgent64.needArm64Support(WrapCoverAdActivity.this,pkg)) {
                    AppManager.launchApp(pkg, userId);
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CloneAgent64 agent64 = new CloneAgent64(WrapCoverAdActivity.this);
                            if (agent64.hasSupport() && agent64.isCloned(pkg, userId)) {
                                MLogs.d(TAG, " launch from agent");
                                agent64.launchApp(pkg, userId);
                            } else {
                                AppManager.launchApp(pkg,userId);
                            }
                            agent64.destroy();
                        }
                    }).start();
                }
                AppMonitorService.onCoverAdClosed(pkg, userId);
                finish();
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onError(String error) {

            }
        });
//        if (adLoader != null && adLoader.hasValidCache()) {
//            adLoader.loadAd(1, new IAdLoadListener() {
//                @Override
//                protected void onAdLoaded(IAdAdapter ad) {
//                    ad.show();
//                }
//
//                @Override
//                protected void onAdClosed(IAdAdapter ad) {
//                    MLogs.d("woriiir");
//                    MLogs.d(WrapCoverAdActivity.class.getName() + " onAdClosed");
//                    CloneManager.launchApp(pkg, userId);
//                    finish();
//                }
//
//                @Override
//                protected void onAdListLoaded(List<IAdAdapter> ads) {
//
//                }
//
//                @Override
//                protected void onError(String error) {
//
//                }
//            });
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
