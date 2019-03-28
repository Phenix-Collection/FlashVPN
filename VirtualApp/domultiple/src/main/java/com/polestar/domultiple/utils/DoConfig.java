package com.polestar.domultiple.utils;

import android.text.TextUtils;

import com.polestar.domultiple.PolestarApp;

import java.util.HashSet;

/**
 * Created by guojia on 2019/3/27.
 */

public class DoConfig {

    public static final String KEY_INTERCEPT_CLASS = "conf_intercept_class";
    public static final String KEY_ADS_LAUNCH_CTRL = "ads_launch_ctrl";
    public static final String KEY_INTERCEPT_INTERVAL = "conf_intercept_interval";
    private static final String KEY_INTERCEPT_HANDLE_TIME = "conf_intercept_interval";

    private static final int NO_OP = -1;
    public static final int ADS_BLOCK = 0;
    public static final int ADS_TO_COVER = 1;
    public static final int ADS_FORCE_REPLACE = 2;

    private static DoConfig sConfig;

    private HashSet<String> mInterstitialActivitySet;

    private int adsCtrl = NO_OP;
    private long interval;

    private DoConfig(){
        mInterstitialActivitySet = new HashSet<>();
        initData();
    }

    public void initData() {
        mInterstitialActivitySet.clear();
        String conf = PreferencesUtils.getString(PolestarApp.getApp(), KEY_INTERCEPT_CLASS, null);
        if (conf != null) {
            String[] arr = conf.split(";");
            if (arr != null) {
                for (String s:arr) {
                    if (!TextUtils.isEmpty(s)) {
                        mInterstitialActivitySet.add(s);
                    }
                }
            }
        }
        adsCtrl = PreferencesUtils.isAdFree() ? NO_OP : PreferencesUtils.getInt(PolestarApp.getApp(), KEY_ADS_LAUNCH_CTRL, NO_OP);
        interval = PreferencesUtils.getLong(PolestarApp.getApp(), KEY_INTERCEPT_INTERVAL, 30*1000);
    }

    public static synchronized DoConfig get() {
        if (sConfig == null) {
            sConfig = new DoConfig();
        }
        return sConfig;
    }

    public void updateInterceptTime(String pkgKey) {
        PreferencesUtils.putLong(PolestarApp.getApp(), "itc_" + pkgKey, System.currentTimeMillis());
    }

    private long getInterceptTime(String pkgKey) {
        long last = PreferencesUtils.getLong(PolestarApp.getApp(), "itc_" + pkgKey, 0);
        return last;
    }

    public boolean isHandleInterstitial(String pkgKey, String clz) {
        boolean exist = mInterstitialActivitySet.contains(clz) && adsCtrl != NO_OP;
        if (exist && !TextUtils.isEmpty(pkgKey)) {
            if (System.currentTimeMillis() - getInterceptTime(pkgKey) < interval)  {
                return false;
            }
        }
        return exist;
    }

    public boolean isPolicyInterstitialBlock() {
        return adsCtrl == ADS_BLOCK || adsCtrl == ADS_FORCE_REPLACE;
    }

    public int getInterstitialAdsCtl() {
        return adsCtrl;
    }
}
