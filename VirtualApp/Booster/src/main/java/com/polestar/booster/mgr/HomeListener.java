package com.polestar.booster.mgr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.polestar.booster.BoosterLog;

public class HomeListener {

    public KeyFun mKeyFun;
    public Context mContext;
    public IntentFilter mHomeBtnIntentFilter = null;
    public HomeBtnReceiver mHomeBtnReceiver = null;

    public HomeListener(Context context) {
        mContext = context;
        mHomeBtnIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mHomeBtnReceiver = new HomeBtnReceiver();
    }

    public void startListen(KeyFun keyFun) {
        if (mContext != null )
            mContext.registerReceiver(mHomeBtnReceiver, mHomeBtnIntentFilter);
        else
            BoosterLog.log( "mContext is null and startListen fail");
        mKeyFun = keyFun;
    }
    public void stopListen() {
        if (mContext != null && mKeyFun!=null)
            mContext.unregisterReceiver(mHomeBtnReceiver);
        else
            BoosterLog.log(  "mContext is null and stopListen fail");
        mKeyFun = null;
    }

    class HomeBtnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BoosterLog.log("onReceive " + intent);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null) {
                    if(null != mKeyFun ){
                        if (reason.equals("homekey")) {
                            //按Home按键
                            mKeyFun.home();
                        } else if (reason.equals("recentapps")) {
                            //最近任务键也就是菜单键
                            mKeyFun.recent();
                        } else if (reason.equals("assist")) {
                            //常按home键盘
                            mKeyFun.longHome();
                        }
                    }
                }
            }
        }
    }

    public interface KeyFun {
        public void home();
        public void recent();
        public void longHome();
    }
}