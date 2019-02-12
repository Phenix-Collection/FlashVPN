package in.dualspace.cloner.components.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by DualApp on 2017/7/23.
 */

public class CrashReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals("appclone.intent.action.SHOW_CRASH_DIALOG")) {
            try {
                String pName = intent.getStringExtra("package");
                boolean forground = intent.getBooleanExtra("forground", false);
                int tag = intent.getIntExtra("tag", 0);
                MLogs.logBug("CrashReceiver onReceive crash: " + pName + " forground: " + forground);
                CrashReport.startCrashReport();
                CrashReport.setUserSceneTag(context, tag);
                Throwable ex = (Throwable) intent.getSerializableExtra("exception");
                EventReporter.reportCrash(ex, pName,forground);
                CrashReport.postCatchedException(ex);
                if(forground) {
                    //ToastUtils.ToastDefultLong(context, context.getString(R.string.crash_hint));
                }
            } catch (Exception e) {
                MLogs.logBug("Error in CrashReceiver " + MLogs.getStackTraceString(e));
            }

        }
    }
}
