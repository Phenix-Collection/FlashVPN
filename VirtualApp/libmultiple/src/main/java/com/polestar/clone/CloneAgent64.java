package com.polestar.clone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;

import com.polestar.clone.helper.utils.VLog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mirror.android.content.pm.ApplicationInfoL;

/**
 * Created by guojia on 2018/5/23.
 */

public class CloneAgent64 {
    private static ICloneAgent iCloneAgent;
    private Context mContext;
    private static final String TAG = "CloneAgent";
//    static private CloneAgent64 sInstance;
//
//    static public synchronized CloneAgent64 getsInstance(Context context) {
//        if( sInstance == null) {
//            sInstance = new CloneAgent64(context);
//        }
//        return  sInstance;
//    }

    public void createClone(String pkg, int userId){
        try {
            if(getAgent()!= null){
                getAgent().createClone(pkg, userId);
            }
        } catch (Exception ex){

        }
    }

    public boolean hasSupport() {
        boolean ret = false;
        try {
            ret = getAgent() != null;
        } catch (Exception ex){

        }
        VLog.d(TAG, "hasSupport: " + ret);
        return ret;
    }

    public void deleteClone(String pkg, int userId){
        try {
            if (getAgent() != null) {
                getAgent().deleteClone(pkg, userId);
            }
        }catch (Exception ex){

        }
    }
    public void launchApp(String pkg, int userId){
        try {
            if(getAgent()!= null){
                getAgent().launchApp(pkg, userId);
            }
        }catch (Exception ex){

        }

    }
    public boolean isNeedUpgrade(String pkg){
        try {
            if(getAgent()!= null){
                return getAgent().isNeedUpgrade(pkg);
            }
        }catch (Exception ex){

        }
        return false;
    }

    public void upgradeApp(String pkg){
        try {
            if(getAgent()!= null){
                getAgent().upgradeApp(pkg);
            }
        }catch (Exception ex){

        }
    }

    public boolean isCloned(String pkg, int userId){
        try{
            if(getAgent()!= null){
               return getAgent().isCloned(pkg, userId);
            }
        }catch (Exception ex){

        }
        return false;
    }

    private static final BlockingQueue<Integer> syncQueue = new LinkedBlockingQueue<Integer>(1);
    private static ServiceConnection agentServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                iCloneAgent = ICloneAgent.Stub.asInterface(service);
                iCloneAgent.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        iCloneAgent = null;
                    }
                }, 0);
                syncQueue.put(1);
            } catch (Exception e) {
                // will never happen, since the queue starts with one available slot
            }
            VLog.d("CloneAgent", "connected "+ name);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            iCloneAgent = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {

        }
    };

    static public boolean needArm64Support(Context context, String pkg){
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkg, 0);
            String clonePrimaryAbi = ApplicationInfoL.primaryCpuAbi.get(ai);
            String cloneSecondAbi = ApplicationInfoL.secondaryCpuAbi.get(ai);
            VLog.d(TAG, "NeedAbiSupport: " +  " pri: " + clonePrimaryAbi + " sec: " + cloneSecondAbi);
            if (clonePrimaryAbi == null) {
                return false;
            }
            if (clonePrimaryAbi.contains("armeabi")) {
                return false;
            }
            if (cloneSecondAbi == null || !cloneSecondAbi.contains("armeabi")) {
                return true;
            }
        }catch (Exception ex){

        }
        return false;
    }

    public void syncPackageSetting(String pkg, int userId, CustomizeAppData data) {
        try{
            if(getAgent()!= null){
                getAgent().syncPackageSetting(pkg, userId, data);
            }
        }catch (Exception ex){

        }
    }

    private ICloneAgent getAgent() {
        if (iCloneAgent != null) {
            return  iCloneAgent;
        }
        String supportPkg = mContext.getPackageName()+".arm64";
        try{
            ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(supportPkg, 0);
        }catch (PackageManager.NameNotFoundException ex) {
            return  null;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new RuntimeException("Cannot getAgent in main thread!");
        }
        ComponentName comp = new ComponentName(supportPkg, CloneAgentService.class.getName());
        Intent intent = new Intent();
        intent.setComponent(comp);
        VLog.d("CloneAgent", "bindService intent "+ intent);
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
        timer.schedule(task, 8000);
        try {
            mContext.bindService(intent,
                    agentServiceConnection,
                    Context.BIND_AUTO_CREATE);
            syncQueue.take();
        }catch (Exception ex) {

        }
        return iCloneAgent;
    }

    public CloneAgent64(Context context) {
        mContext = context;
    }

    public void destroy() {
//        if (iCloneAgent != null ) {
//            mContext.unbindService(agentServiceConnection);
//        }
    }
}
