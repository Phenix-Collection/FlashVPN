package winterfell.flash.vpn.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Iterator;

import winterfell.flash.vpn.utils.MLogs;

public class NetworkUtils {

    public static boolean hasVpnConnected() {
        try {
            Iterator v1 = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            do {
                if(!v1.hasNext()) {
                    return false;
                }

                NetworkInterface networkInterface = (NetworkInterface)v1.next();
                if(!networkInterface.isUp()) {
                    continue;
                }

                if(!networkInterface.getName().contains("tun0")) {
                    continue;
                }

                break;
            }
            while(true);
        }
        catch(Exception v0) {
            MLogs.d(v0.toString());
        }

        return true;
    }

    public static boolean isNetConnected(Context arg7) {
        boolean v0_2;
        Object v0;
        if(Build.VERSION.SDK_INT < 21) {
            v0 = arg7.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo v3 = ((ConnectivityManager)v0).getNetworkInfo(1);
            NetworkInfo v0_1 = ((ConnectivityManager)v0).getNetworkInfo(0);
            if(v3 != null && (v3.isConnected()) || v0_1 != null && (v0_1.isConnected())) {
                v0_2 = true;
                return v0_2;
            }

            v0_2 = false;
        }
        else {
            v0 = arg7.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] v4 = ((ConnectivityManager)v0).getAllNetworks();
            if(v4 != null && v4.length > 0) {
                int v5 = v4.length;
                int v3_1;
                for(v3_1 = 0; v3_1 < v5; ++v3_1) {
                    NetworkInfo v6 = ((ConnectivityManager)v0).getNetworkInfo(v4[v3_1]);
                    if(v6 != null && (v6.isConnected())) {
                        return true;
                    }
                }

                return false;
            }

            v0_2 = false;
        }

        return v0_2;
    }

    public static boolean isWifiConnected(Context arg3) {
        boolean v0 = true;
        NetworkInfo v1 =  getActiveNetworkInfo( getConnectivityManager(arg3));
        if(v1 == null || !v1.isConnected() || v1.getType() != 1) {
            v0 = false;
        }

        return v0;
    }

    private static ConnectivityManager getConnectivityManager(Context arg1) {
        Object v0_1 = null;
        if(arg1 == null) {
            ConnectivityManager v0 = null;
        }
        else {
            v0_1 = arg1.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        return ((ConnectivityManager)v0_1);
    }

    private static NetworkInfo getActiveNetworkInfo(ConnectivityManager arg2) {
        NetworkInfo v0 = null;
        if(arg2 != null) {
            try {
                v0 = arg2.getActiveNetworkInfo();
            }
            catch(Exception v1) {
                v1.printStackTrace();
            }
        }

        return v0;
    }

    public static boolean isMobileConnected(Context arg2) {
        NetworkInfo v0 = getActiveNetworkInfo(getConnectivityManager(arg2));
        boolean v0_1 = v0 == null || !v0.isConnected() || v0.getType() != 0 ? false : true;
        return v0_1;
    }
}
