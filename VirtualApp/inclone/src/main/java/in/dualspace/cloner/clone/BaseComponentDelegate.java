package in.dualspace.cloner.clone;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.delegate.ComponentDelegate;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserHandle;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.components.AppMonitorService;
import in.dualspace.cloner.db.CloneModel;
import in.dualspace.cloner.db.DBManager;
import in.dualspace.cloner.utils.MLogs;
import infi.dualspace.cloner.IAppMonitor;

/**
 * Created by guojia on 2019/2/22.
 */

public abstract class BaseComponentDelegate implements ComponentDelegate {
    private IAppMonitor uiAgent;
    protected HashSet<String> notificationPkgs = new HashSet<>();


    public BaseComponentDelegate() {
        asyncInit();
    }

    protected IAppMonitor getAgent() {
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
        timer.schedule(task, 1000);
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

    private void asyncInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!DualApp.isArm64()) {
                    List<CloneModel> list = DBManager.queryAppList(DualApp.getApp());
                    for (CloneModel app : list) {
                        if (app.getNotificationEnable()) {
                            notificationPkgs.add(CloneManager.getMapKey(app.getPackageName(), app.getPkgUserId()));
                        }
                    }
                }
                uiAgent = getAgent();
            }
        }).start();

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
}
