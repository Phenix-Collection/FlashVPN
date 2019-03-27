package mochat.multiple.parallel.whatsclone.component;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import com.polestar.clone.CustomizeAppData;
import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.delegate.ComponentDelegate;
import com.polestar.clone.os.VUserHandle;

import mochat.multiple.parallel.whatsclone.IAppMonitor;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.db.DbManager;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import mochat.multiple.parallel.whatsclone.utils.AppManager;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.WhatsConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by guojia on 2016/12/16.
 */

public class MComponentDelegate implements ComponentDelegate {

    private HashSet<String> pkgs = new HashSet<>();
    private IAppMonitor uiAgent;
    public void asyncInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!MApp.isArm64()) {
                    List<AppModel> list = DbManager.queryAppList(MApp.getApp());
                    for (AppModel app : list) {
                        if (app.getNotificationEnable()) {
                            pkgs.add(AppManager.getMapKey(app.getPackageName(), app.getPkgUserId()));
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

    @Override
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(String pkg, int userId) {
//        MLogs.d("beforeActivityResume " + pkg);
//        //if (PreferencesUtils.isLockerEnabled(VirtualCore.get().getContext())) {
//            AppLockMonitor.getInstance().onActivityResume(pkg, userId);
//        //}
    }

    @Override
    public void beforeActivityPause(String pkg, int userId) {
//        MLogs.d("beforeActivityPause " + pkg);
//       // if (PreferencesUtils.isLockerEnabled(VirtualCore.get().getContext())) {
//            AppLockMonitor.getInstance().onActivityPause(pkg, userId);
//       // }
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
                    MLogs.logBug(ex);
                }
            }
        }).start();
    }

    @Override
    public void afterActivityPause(Activity activity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    getAgent().onAppSwitchBackground(activity.getPackageName(), VUserHandle.myUserId());
                }catch (Exception ex) {
                    MLogs.logBug(ex);
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
        String key = AppManager.getMapKey(pkg, userId);
        MLogs.d("isNotificationEnabled pkg: " + key + " " + pkgs.contains(key));
        if ( pkgs.contains(key) ) {
            return  true;
        } else if(MApp.isArm64()) {
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
        PreferencesUtils.setEncodedPatternPassword(MApp.getApp(),lockKey);
        PreferencesUtils.setAdFree(adFree);
        PreferencesUtils.setLockInterval(lockInterval);
    }

    @Override
    public boolean handleStartActivity(String name) {
        if (WhatsConfig.get().isHandleInterstitial(name)  ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getAgent().onAdsLaunch(VClientImpl.get().getCurrentPackage(), VUserHandle.myUserId(), name);
                    }catch (Exception ex) {

                    }
                }
            }).start();
            return WhatsConfig.get().isPolicyInterstitialBlock();
        }
        return false;
    }

    private IAppMonitor getAgent() {
        if (uiAgent != null) {
            return  uiAgent;
        }
        String targetPkg = MApp.getApp().getPackageName();
        if (targetPkg.endsWith(".arm64")) {
            targetPkg = targetPkg.replace(".arm64","");
        }
        try{
            ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(targetPkg, 0);
        }catch (PackageManager.NameNotFoundException ex) {
            MLogs.logBug(ex.toString());
            return  null;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("Cannot getAgent in main thread!");
        }
        ComponentName comp = new ComponentName(targetPkg, AppMonitorService.class.getName());
        Intent intent = new Intent();
        intent.setComponent(comp);
        MLogs.d("AppMonitor", "bindService intent "+ intent);
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
            MLogs.d("CloneAgent", "connected "+ name);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            uiAgent = null;
        }
    };
}
