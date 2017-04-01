package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.ToastUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.StatService;

/**
 * Created by hxx on 9/21/16.
 */

public class CrashReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals("appclone.intent.action.SHOW_CRASH_DIALOG")) {
                String pName = intent.getStringExtra("package");
                boolean forground = intent.getBooleanExtra("forground", false);
                MLogs.logBug("CrashReceiver onReceive crash: " + pName + " forground: " + forground);
                MTAManager.reportCrash(context, pName,forground);
                if(forground) {
                    ToastUtils.ToastDefultLong(context, context.getString(R.string.crash_hint));
                }
            }
    }
}
