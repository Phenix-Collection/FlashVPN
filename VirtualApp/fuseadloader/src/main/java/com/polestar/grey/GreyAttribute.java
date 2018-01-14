package com.polestar.grey;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.compat.BuildConfig;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.polestar.ad.AdLog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nativesdk.ad.common.app.Constants;
import nativesdk.ad.common.common.network.data.FetchAdResult;
import nativesdk.ad.common.common.utils.L;
import nativesdk.ad.common.common.utils.Utils;
import nativesdk.ad.common.database.AdInfo;
import nativesdk.ad.common.database.AvDatabaseUtils;
import nativesdk.ad.common.utils.DeviceUtil;
import nativesdk.ad.common.utils.PreferenceUtils;

/**
 * Created by guojia on 2018/1/1.
 */

public class GreyAttribute {
    private static final String TAG = "GreyAttribute";
    //TODO get from remote config
    private final static String FETCH_URL =
            "http://api.c.avazunativeads.com/appwall?sourceid=29026&adpkg={adpkg}&req_type=3&market=google"
                    +"&deviceid={devId}&sdkversion=2.2.7.092217&pkg={mypkg}&ua={ua}&os=android&language={lang}&" +
                    "reqId={reqid}&maid={maid}&gpid={gpid}";
    //fetch ad and do click, and get referrer
    public static void checkAndClick(final Context ctx, final String pkg) {
        AdLog.d(TAG, "checkAndClick");
        new Thread(new Runnable() {
            @Override
            public void run() {
                doCheckAndClick(ctx, pkg);
            }
        },"check-click").start();
    }
    private static void doCheckAndClick(Context ctx, String pkg) {
        try {
            String devId = DeviceUtil.getDeviceId(ctx);
            String mypkg = BuildConfig.APPLICATION_ID;
            String lang = Utils.getLanguage();
            String reqId = UUID.randomUUID().toString();
            String aid = DeviceUtil.getAndroidId(ctx);
            String ua = URLEncoder.encode(PreferenceUtils.getUserAgent(ctx), "UTF-8");
            String gpid = DeviceUtil.getGoogleAdvertisingId(ctx);
            String reqUrl = FETCH_URL.replace("{adpkg}", pkg).replace("{devId}",devId).replace("{mypkg}",mypkg)
                    .replace("{ua}", ua).replace("{lang}",lang).replace("{reqid}",reqId).replace("{maid}",aid)
                    .replace("{gpid}",gpid);

            AdLog.d(TAG,"ReqURL: " + reqUrl);
            String ret = doGET(reqUrl, PreferenceUtils.getUserAgent(ctx));

            if (TextUtils.isEmpty(ret)) {
                return;
            }
            try {
                Gson gson = new Gson();
                FetchAdResult ads = gson.fromJson(ret, FetchAdResult.class);
                if (!FetchAdResult.isFailed(ads)) {
                    List<AdInfo> adInfoList = new ArrayList<AdInfo>();
                    for (FetchAdResult.Ad data : ads.ads.a) {
                        AdInfo info = new AdInfo(data, Constants.ApxAdType.APPWALL);
                        AdLog.d(TAG, "AdInfo: " + info.clkurl);

                    }
                } else {
                    AdLog.d(TAG, "failed to load");
                }
            } catch (Throwable e) {
                L.e(e);
            }

        }catch (Throwable ex) {
            AdLog.d(ex);
        }

        //String deviceId =

    }

    private static String doGET(String _url, String userAgent) {
        L.d("[doGET] " + _url);
        try {
            URL url = new URL(_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(30000);
            conn.setConnectTimeout(30000);
            conn.setDoInput(true);
            conn.setRequestProperty("User-Agent", userAgent);
            int rspCode = conn.getResponseCode();
            L.d("rspCode" + rspCode);
//            if (rspCode >= 400) {
//                return null;
//            }

            byte[] buffer = new byte[8192];
            BufferedInputStream bis = null;
            ByteArrayOutputStream baos = null;
            try {
                bis = new BufferedInputStream(conn.getInputStream());
                baos = new ByteArrayOutputStream();
                int len;
                while ((len = bis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                final String ret = new String(baos.toByteArray());
                L.d("[doGET] " + ret);
                return ret;
            } finally {
                if (bis != null) {
                    bis.close();
                }

                if (baos != null) {
                    baos.close();
                }
            }
        } catch (Throwable e) {
            L.e(e);
        }
        return null;
    }

    //send referrer to package
    public static void sendAttributor(String pkg) {

    }
}
