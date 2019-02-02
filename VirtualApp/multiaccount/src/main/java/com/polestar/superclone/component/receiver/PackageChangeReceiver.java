package com.polestar.superclone.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.utils.AppManager;
import com.polestar.superclone.utils.CloneHelper;
import com.polestar.superclone.utils.MLogs;

/**
 * Created by yxx on 2016/9/7.
 */
public class PackageChangeReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String packageName = intent.getDataString();
        if (packageName != null && packageName.startsWith("package:")) {
            packageName = packageName.replaceFirst("package:", "");
        }
        boolean replacing = intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) ||
                intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        MLogs.e("PackageChange: " + intent.getAction() + " packageName = " + packageName + " replacing: " + replacing);
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            DbManager.notifyChanged();
            if (!replacing) {
                CloneHelper.getInstance(context).unInstallApp(context, packageName);
            }
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            DbManager.notifyChanged();
            final String pkg = packageName;
            boolean installed = false;
            try{
                installed = VirtualCore.get() != null && VirtualCore.get().isAppInstalled(packageName);
            }catch (Exception e) {
                MLogs.logBug(VLog.getStackTraceString(e));
            }
            if( replacing && installed) {
                MLogs.d("app install: replacing upgrade ");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppManager.upgradeApp(pkg);
                    }
                }).start();
            }
            if (!replacing && !installed) {
                MLogs.d("call booster onInstall");
//                BoostMgr.getInstance(context).onInstall(pkg);
            }
        }
    }
}
