package com.polestar.multiaccount.firebase;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;

/**
 * Created by hxx on 8/12/16.
 */
public class PbFirebaseInstanceIdReceiver extends WakefulBroadcastReceiver {

//    private static final String ACTION_REGISTRATION
//            = "com.google.android.c2dm.intent.REGISTRATION";
//    private static final String ACTION_RECEIVE
//            = "com.google.android.c2dm.intent.RECEIVE";

    @Override
    public void onReceive(Context context, Intent intent) {
        MLogs.d("onReceive");

        // 如果用户关闭PB推送，则收到广播以后中断该广播,
        // 否则继续传递给Firebase默认的receiver处理（这里确保我们能先于Firebase sdk收到该广播）
        if (!PreferencesUtils.getBoolean(context, AppConstants.KEY_SERVER_PUSH, true)) {
            MLogs.d("abortBroadcast");
            abortBroadcast();
        }
    }

}
