package winterfell.flash.vpn.utils;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

import winterfell.flash.vpn.BuildConfig;

public class BugReporter {

    static public void init(Context context) {
        //Bugly
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        MLogs.e("bugly channel: " + channel);
        MLogs.e("versioncode: " + BuildConfig.VERSION_CODE + ", versionName:" + BuildConfig.VERSION_NAME);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(channel);
        CrashReport.initCrashReport(context, "cfcb750769", BuildConfig.DEBUG, strategy);
        // close auto report, manual control
        CrashReport.closeCrashReport();
    }
}
