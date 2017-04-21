package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.MLogs;

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
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
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
            if( replacing && VirtualCore.get().isAppInstalled(packageName)) {
                MLogs.d("app install: replacing upgrade ");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppManager.upgradeApp(pkg);
                    }
                }).start();
            }
        }
    }
}
