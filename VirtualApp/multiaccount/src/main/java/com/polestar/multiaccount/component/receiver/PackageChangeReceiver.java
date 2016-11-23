package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.Logs;

/**
 * Created by yxx on 2016/9/7.
 */
public class PackageChangeReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            Logs.e("app uninstall: packageName = " + packageName);
            if (packageName != null && packageName.startsWith("package:")) {
                packageName = packageName.replaceFirst("package:", "");
            }
            DbManager.notifyChanged();
            CloneHelper.getInstance(context).unInstallApp(context, packageName);
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            DbManager.notifyChanged();
            Logs.e("app install: packageName = " + packageName);
        }
    }
}
