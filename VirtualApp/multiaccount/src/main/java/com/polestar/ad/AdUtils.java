package com.polestar.ad;

import android.content.Context;
import android.provider.Settings;

import com.polestar.multiaccount.MApp;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import nativesdk.ad.common.app.Constants;
import nativesdk.ad.common.common.network.NetworkUtils;
import nativesdk.ad.common.database.AdInfo;
import nativesdk.ad.common.database.AvDatabaseUtils;
import nativesdk.ad.common.manager.AnalyticsManager;


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

    public static void uploadWallImpression(boolean hasClick) {
        List<AdInfo> mDataList =
                AvDatabaseUtils.getCacheAppWallData(MApp.getApp(), 20, Constants.ActivityAd.SORT_ALL);
        UUID mImpid = UUID.randomUUID();
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
            AnalyticsManager.getInstance(MApp.getApp()).doUpload(sb.toString());
            if (hasClick) {
                NetworkUtils.reportTrueClick(MApp.getApp(), mDataList.get(0).noticeUrl);
            }
        }
    }
}
