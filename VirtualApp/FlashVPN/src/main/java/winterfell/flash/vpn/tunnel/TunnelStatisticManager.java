package winterfell.flash.vpn.tunnel;

import java.net.InetSocketAddress;
import java.util.HashMap;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;

public class TunnelStatisticManager {
//    public static class TunnelStatistic {
//        String mSsServer;
//        String mToServer;
//        long mTunnelEstablishTime;
//    }

    private String mLocale;
    //private HashMap<String, HashMap<String, TunnelStatistic>> mStatistic = new HashMap();
    private HashMap<String, Long> mTunnelEstablishTime;
    private TunnelStatisticManager() {
        mLocale = FlashApp.getApp().getResources().getConfiguration().locale.toString();
        mTunnelEstablishTime = new HashMap<>();
    }
    private static TunnelStatisticManager sInstance = null;
    public static TunnelStatisticManager getInstance() {
        if (sInstance == null) {
            sInstance = new TunnelStatisticManager();
        }
        return sInstance;
    }

    private String getKey(InetSocketAddress socketAddress) {
        return socketAddress.getAddress().toString();
    }

    public void setEstablishTime(InetSocketAddress socketAddress, long timeInMilli) {
        synchronized (this) {
            String add = getKey(socketAddress);
            MLogs.d("TunnelStatisticManager-- setEstablishTime " + socketAddress.toString() + " " + add + " " + timeInMilli);
            Long existOne = mTunnelEstablishTime.get(add);
            if (existOne == null) {
                mTunnelEstablishTime.put(add, timeInMilli);
            } else {
                mTunnelEstablishTime.put(add, (timeInMilli + existOne) / 2);
            }
        }
    }

    public void clearEstablishTimes() {
        synchronized (this) {
            mTunnelEstablishTime.clear();
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
}
