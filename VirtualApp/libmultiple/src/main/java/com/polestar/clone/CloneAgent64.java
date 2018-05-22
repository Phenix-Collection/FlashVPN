package com.polestar.clone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.lody.virtual.helper.utils.VLog;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Created by guojia on 2018/5/23.
 */

public class CloneAgent64 {
    private ICloneAgent iCloneAgent;
    private Context mContext;
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
        try {
            return getAgent() != null;
        } catch (Exception ex){

        }
        return false;
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

    private final BlockingQueue<Integer> syncQueue = new LinkedBlockingQueue<Integer>(1);
    ServiceConnection agentServiceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                iCloneAgent = ICloneAgent.Stub.asInterface(service);
                syncQueue.put(1);
            } catch (InterruptedException e) {
                // will never happen, since the queue starts with one available slot
            }
            VLog.d("CloneAgent", "connected "+ name);
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            iCloneAgent = null;
        }
    };

    private ICloneAgent getAgent() {
        if (iCloneAgent != null) {
            return  iCloneAgent;
        }
        ComponentName comp = new ComponentName(mContext.getPackageName()+".arm64", CloneAgentService.class.getName());
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
        timer.schedule(task, 5000);
        mContext.bindService(intent,
                agentServiceConnection,
                Context.BIND_AUTO_CREATE);
        try {
            syncQueue.take();
        }catch (Exception ex) {

        }
        return iCloneAgent;
    }
    public CloneAgent64(Context context) {
        mContext = context;
    }
}
