package com.polestar.clone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.utils.VLog;

/**
 * Created by guojia on 2017/6/11.
 */

public class StubService extends Service {
    private static final String TAG = "StubService";

    public static void startup(Context context, int vpid) {
        VLog.d(TAG, "Startup for : StubService$S" + vpid);
        try {
            Intent i = new Intent();
            i.setClassName(VirtualCore.get().getHostPkg(), StubManifest.getStubServiceName(vpid));
            context.startService(i);
        }catch (Exception e){
            VLog.logbug(TAG, VLog.getStackTraceString(e));
        }
    }

    public static void stop(Context context, int vpid) {
        VLog.d(TAG, "stop for : StubService$S" + vpid);
        try {
            Intent i = new Intent();
            i.setClassName(VirtualCore.get().getHostPkg(), StubManifest.getStubServiceName(vpid));
            context.stopService(i);
        }catch (Exception e){
            VLog.logbug(TAG, VLog.getStackTraceString(e));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //startup(this);
        VLog.d(TAG, this.getClass().getName() + " onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        VLog.logbug(TAG, this.getClass().getName() + " onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VLog.d(TAG, this.getClass().getName() + " onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VLog.d(TAG, this.getClass().getName() + " onStartCommand");
        return START_STICKY;
    }

    public static class S0 extends StubService {
    }

    public static class S1 extends StubService {
    }

    public static class S2 extends StubService {
    }

    public static class S3 extends StubService {
    }

    public static class S4 extends StubService {
    }

    public static class S5 extends StubService {
    }

    public static class S6 extends StubService {
    }

    public static class S7 extends StubService {
    }

    public static class S8 extends StubService {
    }

    public static class S9 extends StubService {
    }

    public static class S10 extends StubService {
    }

    public static class S11 extends StubService {
    }

    public static class S12 extends StubService {
    }

    public static class S13 extends StubService {
    }

    public static class S14 extends StubService {
    }

    public static class S15 extends StubService {
    }

    public static class S16 extends StubService {
    }

    public static class S17 extends StubService {
    }

    public static class S18 extends StubService {
    }

    public static class S19 extends StubService {
    }

    public static class S20 extends StubService {
    }

    public static class S21 extends StubService {
    }

    public static class S22 extends StubService {
    }

    public static class S23 extends StubService {
    }

    public static class S24 extends StubService {
    }

    public static class S25 extends StubService {
    }

    public static class S26 extends StubService {
    }

    public static class S27 extends StubService {
    }

    public static class S28 extends StubService {
    }

    public static class S29 extends StubService {
    }

    public static class S30 extends StubService {
    }

    public static class S31 extends StubService {
    }

    public static class S32 extends StubService {
    }

    public static class S33 extends StubService {
    }

    public static class S34 extends StubService {
    }

    public static class S35 extends StubService {
    }

    public static class S36 extends StubService {
    }

    public static class S37 extends StubService {
    }

    public static class S38 extends StubService {
    }

    public static class S39 extends StubService {
    }

    public static class S40 extends StubService {
    }

    public static class S41 extends StubService {
    }

    public static class S42 extends StubService {
    }

    public static class S43 extends StubService {
    }

    public static class S44 extends StubService {
    }

    public static class S45 extends StubService {
    }

    public static class S46 extends StubService {
    }

    public static class S47 extends StubService {
    }

    public static class S48 extends StubService {
    }

    public static class S49 extends StubService {
    }
}