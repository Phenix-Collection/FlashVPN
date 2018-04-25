package mochat.multiple.parallel.whatsclone.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import com.tencent.bugly.crashreport.CrashReport;

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
                try {
                    String pName = intent.getStringExtra("package");
                    boolean forground = intent.getBooleanExtra("forground", false);
                    int tag = intent.getIntExtra("tag", 0);
                    MLogs.logBug("CrashReceiver onReceive crash: " + pName + " forground: " + forground);
                    EventReporter.reportCrash(context, pName,forground);
                    CrashReport.startCrashReport();
                    CrashReport.setUserSceneTag(context, tag);
                    Throwable ex = (Throwable) intent.getSerializableExtra("exception");
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
