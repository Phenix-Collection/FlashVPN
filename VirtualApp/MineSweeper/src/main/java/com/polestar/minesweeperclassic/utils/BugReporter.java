package com.polestar.minesweeperclassic.utils;

import android.content.Context;

import com.polestar.minesweeperclassic.BuildConfig;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class BugReporter {

    static public void init(Context context) {
        //Bugly
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        MLogs.e("bugly channel: " + channel);
        MLogs.e("versioncode: " + BuildConfig.VERSION_CODE + ", versionName:" + BuildConfig.VERSION_NAME);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(channel);
        CrashReport.initCrashReport(context, "174fd16614", BuildConfig.DEBUG, strategy);
        // close auto report, manual control
        CrashReport.closeCrashReport();
    }
}
