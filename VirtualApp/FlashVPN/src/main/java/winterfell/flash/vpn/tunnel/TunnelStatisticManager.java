package winterfell.flash.vpn.tunnel;

import android.os.Handler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.RemoteConfig;

public class TunnelStatisticManager {
//    public static class TunnelStatistic {
//        String mSsServer;
//        String mToServer;
//        long mTunnelEstablishTime;
//    }

    private String mLocale;
    private Handler mHandler;
    //private HashMap<String, HashMap<String, TunnelStatistic>> mStatistic = new HashMap();
    private HashMap<String, Long> mTunnelEstablishTime;
    private HashMap<String, Long> mTunnelBrokenCount;
    private static ConcurrentHashMap<onSpeedListener, Object> mOnSpeedListeners = new ConcurrentHashMap<onSpeedListener, Object>();

    private TunnelStatisticManager() {
        mLocale = FlashApp.getApp().getResources().getConfiguration().locale.toString();
        mTunnelEstablishTime = new HashMap<>();
        mTunnelBrokenCount = new HashMap<>();
        mHandler = new Handler();
    }
    private static TunnelStatisticManager sInstance = null;
    public static TunnelStatisticManager getInstance() {
        if (sInstance == null) {
            sInstance = new TunnelStatisticManager();
        }
        return sInstance;
    }

    private String getKey(InetSocketAddress socketAddress) {
        return CommonUtils.getIpString(socketAddress);
    }

    public interface onSpeedListener {
        public void onBrokenSpeed(String ip);

        //public void onLogReceived(String logString);
    }

    public void addOnSpeedListener(onSpeedListener listener) {
        if (!mOnSpeedListeners.containsKey(listener)) {
            mOnSpeedListeners.put(listener, 1);
        }
    }

    public void removeOnSpeedListener(onSpeedListener listener) {
        if (mOnSpeedListeners.containsKey(listener)) {
            mOnSpeedListeners.remove(listener);
        }
    }

    private void onBrokenSpeed(final String ip) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onSpeedListener, Object> entry : mOnSpeedListeners.entrySet()) {
                    entry.getKey().onBrokenSpeed(ip);
                }
            }
        });
    }

    public void setEstablishTime(InetSocketAddress socketAddress, long timeInMilli) {
        synchronized (this) {
            String add = getKey(socketAddress);
            MLogs.d("TunnelStatisticManager-- setEstablishTime " + add + " " + timeInMilli);
            Long existOne = mTunnelEstablishTime.get(add);
            if (existOne == null) {
                mTunnelEstablishTime.put(add, timeInMilli);
            } else {
                mTunnelEstablishTime.put(add, (timeInMilli + existOne) / 2);
            }

            clearBrokenCount(socketAddress);
        }
    }

    public void clearEstablishTimes() {
        synchronized (this) {
            mTunnelEstablishTime.clear();
        }
    }

    private void increaseBrokenCount(InetSocketAddress socketAddress) {
        synchronized (this) {
            String key = getKey(socketAddress);
            Long existOne = mTunnelBrokenCount.get(key);
            MLogs.d("TunnelStatisticManager-- increaseBrokenCount " + key + " " + existOne);
            if (existOne == null) {
                mTunnelBrokenCount.put(key, (long)1);
            } else {
                mTunnelBrokenCount.put(key, (existOne + 1));
            }
        }
    }

    private void clearBrokenCount(InetSocketAddress socketAddress) {
        synchronized (this) {
            String key = getKey(socketAddress);
            mTunnelBrokenCount.remove(key);
        }
    }

    private boolean needToReportBroken(InetSocketAddress socketAddress) {
        synchronized (this) {
            String key = getKey(socketAddress);
            Long existOne = mTunnelBrokenCount.get(key);
            if (existOne == null) {
                return false;
            } else {
                if (existOne > RemoteConfig.getLong("report_broken_count")) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public void dump() {
        synchronized (this) {
            MLogs.d("TunnelStatisticManager-- dump");
            for (String serv : mTunnelEstablishTime.keySet()) {
                MLogs.d("serv " + serv + " establish Time is " + mTunnelEstablishTime.get(serv));
            }
        }
    }

    public void eventReport() {
        synchronized (this) {
            for (String serv : mTunnelEstablishTime.keySet()) {
                EventReporter.reportTunnelConnectTime(mLocale, serv, mTunnelEstablishTime.get(serv));
            }
        }
    }

    public void setTunnelBroken(InetSocketAddress server) {
        MLogs.d("TunnelStatisticManager-- setTunnelBroken " + getKey(server));
        increaseBrokenCount(server);
        if (needToReportBroken(server)) {
            onBrokenSpeed(getKey(server));
            clearBrokenCount(server);
        }
    }
}
