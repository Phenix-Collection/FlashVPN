package com.lody.virtual.client.stub;

import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.helper.component.BaseService;
import com.lody.virtual.helper.utils.VLog;

/**
 * @author Lody
 */

public class StubPendingService extends BaseService {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VLog.d("StubPendingService", "onStartCommand " + intent.toString());
        startService(intent);
        stopSelf();
        return START_NOT_STICKY;
    }
}
