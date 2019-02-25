package in.dualspace.cloner.components.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;


/**
 * Created by DualApp on 2017/7/23.
 */

public class WakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        EventReporter.reportWake(DualApp.getApp(), intent.getAction());

//        PackageManager pm = context.getPackageManager();
//        if (pm.getComponentEnabledSetting(new ComponentName(context, SplashActivity.class))
//                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
//            pm.setComponentEnabledSetting(new ComponentName(context, SplashActivity.class),
//                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//        }
    }
}
