package nova.fast.free.vpn.utils;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

import nova.fast.free.vpn.BuildConfig;

public class BugReporter {

    static public void init(Context context) {
        //Bugly
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        MLogs.e("bugly channel: " + channel);
        MLogs.e("versioncode: " + BuildConfig.VERSION_CODE + ", versionName:" + BuildConfig.VERSION_NAME);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(channel);
        CrashReport.initCrashReport(context, "e78c02416e", BuildConfig.DEBUG, strategy);
        // close auto report, manual control
        CrashReport.closeCrashReport();
    }
}
