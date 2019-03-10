package com.polestar.superclone.reward;

import com.polestar.superclone.utils.RemoteConfig;

import java.util.ArrayList;

/**
 * Created by guojia on 2019/3/9.
 */

public class IconAdConfig {
    public int cloneThreshold = 5;
    public long ignoreInterval = 24*3600*1000;
    public long showAfterInstall = 72*3600*1000;

    public IconAdConfig() {
        try {
            String conf = RemoteConfig.getString("conf_icon_ad");
            String arr[] = conf.split(":");
            cloneThreshold = Integer.valueOf(arr[0]);
            ignoreInterval = Long.valueOf(arr[1]) * 1000;
            showAfterInstall = Long.valueOf(arr[2]) * 1000;
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
