package com.example.bluelight.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.bluelight.utils.AlarmUtils;
import com.example.bluelight.utils.PreferenceUtils;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public MyBroadcastReceiver() {
        super();
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    if (PreferenceUtils.isAutoEnableOn(context)) {
                        AlarmUtils.setupAlarm(context);
                    }
                    break;
            }

        }
    }
}

