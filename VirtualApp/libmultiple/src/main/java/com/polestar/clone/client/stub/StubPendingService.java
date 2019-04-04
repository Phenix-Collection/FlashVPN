package com.polestar.clone.client.stub;

import com.polestar.clone.client.env.Constants;
import com.polestar.clone.client.ipc.VActivityManager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;

import com.polestar.clone.helper.utils.VLog;

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
        // _VA_|_from_inner_ marked
        if (intent != null) {
            Intent realIntent = intent.getParcelableExtra(Constants.VA_INTENT_KEY_INTENT);
            int userId = intent.getIntExtra("_VA_|_user_id_", 0);
            if (realIntent != null) {
                VActivityManager.get().startService(null, realIntent, null, userId);
            }
        }
        stopSelf();
        return START_NOT_STICKY;
    }
}
