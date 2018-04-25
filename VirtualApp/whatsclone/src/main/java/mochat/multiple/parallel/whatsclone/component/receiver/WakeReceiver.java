package mochat.multiple.parallel.whatsclone.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.component.PreCloneService;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;

import nativesdk.ad.common.AdSdk;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {
    private static boolean isRegistered;

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        String conf = RemoteConfig.getString(AppConstants.CONF_WALL_SDK);
        boolean av = "all".equals(conf) || "avz".equals(conf);
        if (av) {
            AdSdk.initialize(MApp.getApp(), AppConstants.AV_APP_ID, null);
        }
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        EventReporter.reportActive(MApp.getApp(), false);
        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    PreCloneService.tryClean(context);
                    PreCloneService.tryPreClone(context);
                    isRegistered = false;
                    context.getApplicationContext().unregisterReceiver(this);
                }
            }, filter);
            isRegistered = true;
        }
    }
}
