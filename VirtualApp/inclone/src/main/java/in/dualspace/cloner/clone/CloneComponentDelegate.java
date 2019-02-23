package in.dualspace.cloner.clone;

/**
 * Created by DualApp on 2017/7/16.
 */

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;

import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.delegate.ComponentDelegate;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserHandle;
import com.polestar.clone.CustomizeAppData;
import in.dualspace.cloner.AppConstants;
import infi.dualspace.cloner.IAppMonitor;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.components.AppMonitorService;
import in.dualspace.cloner.db.CloneModel;
import in.dualspace.cloner.db.DBManager;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by DualApp on 2016/12/16.
 */

public class CloneComponentDelegate extends BaseComponentDelegate {
    private static HashSet<String> mInterstitialActivitySet = new HashSet<>();
    static {
        mInterstitialActivitySet.add("com.google.android.gms.ads.AdActivity");
        mInterstitialActivitySet.add("com.mopub.mobileads.MoPubActivity");
        mInterstitialActivitySet.add("com.mopub.mobileads.MraidActivity");
        mInterstitialActivitySet.add("com.mopub.common.MoPubBrowser");
        mInterstitialActivitySet.add("com.mopub.mobileads.MraidVideoPlayerActivity");
        mInterstitialActivitySet.add("com.batmobi.BatMobiActivity");
        mInterstitialActivitySet.add("com.facebook.ads.AudienceNetworkActivity");
        mInterstitialActivitySet.add("com.facebook.ads.InterstitialAdActivity");
        mInterstitialActivitySet.add("com.ironsource.sdk.controller.InterstitialActivity");
        mInterstitialActivitySet.add("com.applovin.adview.AppLovinInterstitialActivity");
    }

    public void addClasses(String[] arr) {
        if (arr != null) {
            for (String s:arr) {
                if (!TextUtils.isEmpty(s)) {
                    mInterstitialActivitySet.add(s);
                }
            }
        }
    }

    public CloneComponentDelegate() {
        super();
    }


    @Override
    public void beforeApplicationCreate(Application application) {

    }

    @Override
    public void afterApplicationCreate(Application application) {

    }

    @Override
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(String pkg, int userId) {
    }

    @Override
    public void beforeActivityPause(String pkg, int userId) {
    }

    @Override
    public void beforeActivityDestroy(Activity activity) {

    }

    @Override
    public void afterActivityCreate(Activity activity) {

    }


    @Override
    public boolean handleStartActivity(String name) {
        if (mInterstitialActivitySet.contains(name)) {
            VLog.d("AppInstrumentation","Starting activity: " + name);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getAgent().onAdsLaunch(VClientImpl.get().getCurrentPackage(), VUserHandle.myUserId(), name);
                    }catch (Exception ex) {

                    }
                }
            }).start();
            return true;
        }
        return false;
    }

    @Override
    public void afterActivityDestroy(Activity activity) {

    }

    @Override
    public void onSendBroadcast(Intent intent) {

    }

    @Override
    public boolean isNotificationEnabled(String pkg, int userId) {
        String key = CloneManager.getMapKey(pkg, userId);
        if ( notificationPkgs.contains(key) ) {
            return  true;
        } else if(DualApp.isArm64()) {
            CustomizeAppData data = CustomizeAppData.loadFromPref(pkg, userId);
            if (data.isNotificationEnable) {
                notificationPkgs.add(key);
                return true;
            }
        }
        return false;
    }

    @Override
    public void reloadSetting(String lockKey, boolean adFree, long lockInterval, boolean quickSwitch) {
        PreferencesUtils.setEncodedPatternPassword(DualApp.getApp(),lockKey);
        PreferencesUtils.setAdFree(adFree);
        PreferencesUtils.setLockInterval(lockInterval);
       //AppLockMonitor.getInstance().reloadSetting(lockKey, adFree, lockInterval);
    }
}
