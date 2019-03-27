package winterfell.flash.vpn;

import android.content.Context;

import com.polestar.task.network.AdApiHelper;

import winterfell.flash.vpn.core.ProxyConfig;
import winterfell.flash.vpn.reward.AppUser;

import winterfell.flash.vpn.utils.CommonUtils;
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
        freePremiumTime = coinToTime(getMyBalance());

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

    private long coinToTime(float coin) {
        return (long)(coin * (float)coinPremiumSecRatio);
    }


    public final static int ACCURACY_SECOND = 0;
    public final static int ACCURACY_MINUTE = 1;
    public final static int ACCURACY_AUTO = 1;

    public String coinToTimeString(float coin, int accuracy) {
        long seconds = coinToTime(coin);
        String timeStr = seconds + "s";
        if (seconds > 60) {
            long second = seconds % 60;
            long min = seconds / 60;
            if (accuracy == ACCURACY_SECOND) {
                timeStr = min + "min " + second + "s";
            } else {
                timeStr = min + "min";
            }
            if (min > 60) {
                min = (seconds / 60) % 60;
                long hour = (seconds / 60) / 60;
                if (accuracy == ACCURACY_SECOND) {
                    timeStr = hour + "hour " + min + "min " + second + "s";
                } else {
                    timeStr = hour + "hour " + min + "min";
                }
                if (hour > 24) {
                    hour = ((seconds / 60) / 60) % 24;
                    long day = (((seconds / 60) / 60) / 24);
                    if (accuracy == ACCURACY_SECOND) {
                        timeStr = day + "day " + hour + "hour " + min + "min " + second + "s";
                    } else {
                        timeStr = day + "day " + hour + "hour " + min + "min";
                    }
                }
            }
        }
        return timeStr;
    }
    public String coinToTimeString(float coin) {
        return coinToTimeString(coin, ACCURACY_AUTO);
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
        freePremiumTime = coinToTime(getMyBalance());
    }

    public long getFreePremiumSeconds() {
        MLogs.d("free premium: " + freePremiumTime);
        return freePremiumTime;
    }

    public void costFreePremiumSec(long sec) {
            freePremiumTime -= sec;
            if (freePremiumTime < 0) freePremiumTime = 0;
    }


    public static boolean isVIP() {
        return PreferenceUtils.getBoolean(FlashApp.getApp(), "is_vip", false);
    }


    public void setVIP(boolean enable) {
        boolean orig = PreferenceUtils.getBoolean(FlashApp.getApp(), "is_vip", false);
        if (orig != enable) {
            PreferenceUtils.putBoolean(FlashApp.getApp(), "is_vip", enable);
            updateSubscribe(enable? AdApiHelper.SUBSCRIBE_STATUTS_VALID: AdApiHelper.SUBSCRIBE_STATUTS_VALID);
        }
    }


    @Override
    public void preloadRewardVideoTask() {
        if (!isVIP()) {
            super.preloadRewardVideoTask();
        } else {
            return;
        }
    }
}
