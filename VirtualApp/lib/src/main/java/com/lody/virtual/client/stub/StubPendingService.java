package com.lody.virtual.client.stub;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.helper.utils.VLog;

/**
 * @author Lody
 */

public class StubPendingService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VLog.d("StubPendingService", "onStartCommand " + intent.toString());
        if (intent != null && intent.getComponent() != null) {
            ComponentName cn  = intent.getComponent();
            if (cn.getClassName().equals(StubPendingService.class.getName())){
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        startService(intent);
        stopSelf();
        return START_NOT_STICKY;
    }
}
