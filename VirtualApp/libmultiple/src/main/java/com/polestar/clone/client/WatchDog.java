package com.polestar.clone.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.polestar.clone.client.env.VirtualRuntime;
import com.polestar.clone.helper.utils.VLog;

/**
 * Created by guojia on 2017/4/22.
 */

public class WatchDog  {
    private Handler mHandler;
    public WatchDog() {
        HandlerThread handlerThread = new HandlerThread("WatchDog");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                VLog.logbug("WatchDog", "TimeOut watching: " + msg.what);
                VirtualRuntime.exit();
            }
        };
    }

    public void feed(int code) {
        mHandler.removeMessages(code);
    }

    public void watch(int code, long delayMilis){
        mHandler.sendMessageDelayed(mHandler.obtainMessage(code), delayMilis);
    }
}
