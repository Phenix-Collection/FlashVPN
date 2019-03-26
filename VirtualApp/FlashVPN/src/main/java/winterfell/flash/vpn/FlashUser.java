package winterfell.flash.vpn;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import winterfell.flash.vpn.core.ProxyConfig;
import winterfell.flash.vpn.reward.AppUser;

import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

public class FlashUser extends AppUser{

    private Context mContext;
    private static FlashUser sInstance;
    private long freePremiumTime;
    private long coinPremiumSecRatio;

    private FlashUser() {
        super();
        mContext = FlashApp.getApp();
        coinPremiumSecRatio = RemoteConfig.getLong("conf_coin_premium_ratio_sec");
        freePremiumTime = (long)(getMyBalance() * (float)coinPremiumSecRatio);

        //只load一次
        Thread t = new Thread() {
            @Override
            public void run() {
                MLogs.d("LocalVpnService-- Load config from file ...");
                try {
                    ProxyConfig.Instance.loadFromFile(mContext.getResources().openRawResource(R.raw.config));
                    MLogs.d("LocalVpnService-- Load done");
                } catch (Exception e) {
                    String errString = e.getMessage();
                    if (errString == null || errString.isEmpty()) {
                        errString = e.toString();
                    }
                    MLogs.d("LocalVpnService-- Load failed with error: %s", errString);
                }
            }
        };

        t.start();
    }

    synchronized public static FlashUser getInstance() {
        if (sInstance == null) {
            sInstance = new FlashUser();
        }
        return sInstance;
    }

    @Override
    public void updateMyBalance(float balance) {
        super.updateMyBalance(balance);
        freePremiumTime = (long)(getMyBalance() * (float)coinPremiumSecRatio);
    }

    public long getFreePremiumSeconds() {
        MLogs.d("free premium: " + freePremiumTime);
        return freePremiumTime;
    }

    public void costFreePremiumSec(long sec) {
            freePremiumTime -= sec;
            if (freePremiumTime < 0) freePremiumTime = 0;
    }


    public boolean isVIP() {
        return PreferenceUtils.getBoolean(FlashApp.getApp(), "is_vip", false);
    }


    public void setVIP(boolean enable) {
        PreferenceUtils.putBoolean(FlashApp.getApp(), "is_vip", enable);
    }
}
