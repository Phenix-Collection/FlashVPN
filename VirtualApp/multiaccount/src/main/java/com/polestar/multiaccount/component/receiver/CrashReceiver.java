package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.multiaccount.R;
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
            MLogs.e("CrashReceiver onReceive");
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals("appclone.intent.action.SHOW_CRASH_DIALOG")) {
                String pName = intent.getStringExtra("package");
                Throwable ex = (Throwable) intent.getSerializableExtra("exception");
                MTAManager.reportCrash(context, pName);
                if (ex != null) {
                    MLogs.e("report crash and app exited.");
                    // bugly:若是导致app退出的崩溃，特殊上报，并标签
                    CrashReport.startCrashReport();
                    CrashReport.setUserSceneTag(context, 32459);
                    CrashReport.postCatchedException(ex);
                    //MTA
                    StatService.reportException(context, ex);
                }
                ToastUtils.ToastDefultLong(context, context.getString(R.string.crash_hint));
            }
    }
}
