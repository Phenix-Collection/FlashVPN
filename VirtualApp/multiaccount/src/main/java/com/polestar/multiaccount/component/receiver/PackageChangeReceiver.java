package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.duapps.ad.base.DuAdNetwork;
import com.polestar.multiaccount.db.DbManager;
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
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            MLogs.e("app uninstall: packageName = " + packageName);
            if (packageName != null && packageName.startsWith("package:")) {
                packageName = packageName.replaceFirst("package:", "");
            }
            DbManager.notifyChanged();
            boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (!replacing) {
                CloneHelper.getInstance(context).unInstallApp(context, packageName);
            }
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            DbManager.notifyChanged();
            DuAdNetwork.onPackageAddReceived(context, intent);
            MLogs.e("app install: packageName = " + packageName);
        }
    }
}
