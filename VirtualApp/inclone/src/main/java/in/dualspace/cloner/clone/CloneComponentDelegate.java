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

public class CloneComponentDelegate implements ComponentDelegate {

    private HashSet<String> pkgs = new HashSet<>();

    public void addClasses(String[] arr) {
        if (arr != null) {
            for (String s:arr) {
                if (!TextUtils.isEmpty(s)) {
                    mInterstitialActivitySet.add(s);
                }
            }
        }
    }

    public void asyncInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!DualApp.isArm64()) {
                    List<CloneModel> list = DBManager.queryAppList(DualApp.getApp());
                    for (CloneModel app : list) {
                        if (app.getNotificationEnable()) {
                            pkgs.add(CloneManager.getMapKey(app.getPackageName(), app.getPkgUserId()));
                        }
                    }
                }
                uiAgent = getAgent();
            }
        }).start();

    }

    @Override
    public void beforeApplicationCreate(Application application) {

    }

    @Override
    public void afterApplicationCreate(Application application) {

    }
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
    public void afterActivityResume(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getAgent().onAppSwitchForeground(activity.getPackageName(), VUserHandle.myUserId());
                }catch (Exception ex) {

                }
            }
        }).start();
    }

    IAppMonitor uiAgent;

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

    private IAppMonitor getAgent() {
        if (uiAgent != null) {
            return  uiAgent;
        }
        String targetPkg = DualApp.getApp().getPackageName();
        if (targetPkg.endsWith(".arm64")) {
            targetPkg = targetPkg.replace(".arm64","");
            boolean foundTarget;
            try{
                ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(targetPkg, 0);
                foundTarget = (ai != null);
            }catch (PackageManager.NameNotFoundException ex) {
                MLogs.logBug(ex.toString());
                foundTarget = false;
            }
            if (!foundTarget) {
                targetPkg = AppConstants.PRIMARY_PKG;
            }
            try{
                ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(targetPkg, 0);
            }catch (PackageManager.NameNotFoundException ex) {
                MLogs.logBug(ex.toString());
                return null;
            }
        }

        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("Cannot getAgent in main thread!");
        }
        ComponentName comp = new ComponentName(targetPkg, AppMonitorService.class.getName());
        Intent intent = new Intent();
        intent.setComponent(comp);
        VLog.d("AppMonitor", "bindService intent "+ intent);
        syncQueue.clear();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    syncQueue.put(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 5000);
        try {
            VirtualCore.get().getContext().bindService(intent,
                    agentServiceConnection,
                    Context.BIND_AUTO_CREATE);
            syncQueue.take();
        }catch (Exception ex) {

        }
        return uiAgent;
    }

    private final BlockingQueue<Integer> syncQueue = new LinkedBlockingQueue<Integer>(1);
    ServiceConnection agentServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                uiAgent = IAppMonitor.Stub.asInterface(service);
                syncQueue.put(1);
            } catch (InterruptedException e) {
                // will never happen, since the queue starts with one available slot
            }
            VLog.d("CloneAgent", "connected "+ name);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            uiAgent = null;
        }
    };

    @Override
    public void afterActivityPause(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getAgent().onAppSwitchBackground(activity.getPackageName(), VUserHandle.myUserId());
                }catch (Exception ex) {

                }
            }
        }).start();

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
        MLogs.d("isNotificationEnabled pkg: " + key + " " + pkgs.contains(key));
        if ( pkgs.contains(key) ) {
            return  true;
        } else if(DualApp.isArm64()) {
            CustomizeAppData data = CustomizeAppData.loadFromPref(pkg, userId);
            if (data.isNotificationEnable) {
                pkgs.add(key);
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
