package com.polestar.ad;

import android.content.Context;
import android.provider.Settings;

import com.polestar.multiaccount.MApp;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import nativesdk.ad.adsdk.analytics.AnalyticsMgr;
import nativesdk.ad.adsdk.app.Constants;
import nativesdk.ad.adsdk.common.utils.L;
import nativesdk.ad.adsdk.database.AdInfo;
import nativesdk.ad.adsdk.database.AvDatabaseUtils;
import nativesdk.ad.adsdk.manager.AnalyticsManager;

/**
 * Created by guojia on 2016/12/11.
 */

public class AdUtils {
    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void uploadWallImpression() {
        List<AdInfo> mDataList =
                AvDatabaseUtils.getCacheAdData(MApp.getApp(), 20, Constants.ActivityAd.SORT_ALL);
        UUID mImpid = UUID.randomUUID();
        boolean hasClick = new Random().nextInt(100) < 10;
        long showTime = 3000 + new Random().nextInt(10000);
        if (mDataList.size() != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(mDataList.get(0).impurls);
            sb.append("&adlist=");
            for (int i = 0; i < mDataList.size(); i++) {
                sb.append(mDataList.get(i).campaignid);
                if (i != (mDataList.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append("&impid=").append(mImpid.toString());
            sb.append("&imppage=appstore");
            sb.append("&showtime=").append(showTime);
            sb.append("&hasclick=").append(hasClick);
            L.d("final url", sb.toString());
            AnalyticsManager.getInstance(MApp.getApp()).doUpload(sb.toString(),
                    Constants.Preference.TYPE_APP_MARKET);
        }
    }
}
