package com.polestar.ad;

/**
 * Created by doriscoco on 2017/1/26.
 */

public class AdControlInfo {
    public int network;
    public int random;
    public long coldDown;

    public static final int NETWORK_WIFI_ONLY = 1;
    public static final int NETWORK_BOTH = 0;

    public AdControlInfo(int network, int random, long coldDown){
        this.network = network;
        this.random = random;
        this.coldDown = coldDown;
    }
}
