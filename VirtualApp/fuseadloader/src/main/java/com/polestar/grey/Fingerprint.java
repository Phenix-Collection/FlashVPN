package com.polestar.grey;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.BuildConfig;

import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created by guojia on 2018/3/17.
 */

public class Fingerprint {

    private final static String FINGERPRINT_FILE = "fingerprint";
    private final static String TAG = "fingerprint";
    private final static String FINGERPRINT_VERSION = BuildConfig.DEBUG? "3" : "100";

    private static String getUniqueId(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences("fingerprint", Context.MODE_PRIVATE);
        String unique = settings.getString("unique_id", "");
        if (TextUtils.isEmpty(unique)) {
            unique = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("unique_id", unique);
            editor.commit();
        }

        return unique;
    }

    public static void genFingerprint(Context ctx, String url, boolean force) {
        File file = new File(ctx.getFilesDir(),FINGERPRINT_FILE);
        if (file.exists() && !force && !BuildConfig.DEBUG) {
            AdLog.d(TAG,"fingerprint exist");
            return ;
        }
        if (ctx.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ctx.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
            AdLog.e(TAG, "insufficient permission");
            //return;
        }

        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos=new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos);
            JSONObject jsonObject = dumpInfo(ctx);
            JSONObject wrapJsonProject = new JSONObject();
            wrapJsonProject.put("phoneInfo", jsonObject.toString());
            wrapJsonProject.put("security", "ilovelinux");
            wrapJsonProject.put("infoVersion", FINGERPRINT_VERSION);
            wrapJsonProject.put("unique",getUniqueId(ctx));
            AdLog.d(TAG, "unique: " + getUniqueId(ctx));
//            if (!BuildConfig.DEBUG) {
                post(wrapJsonProject, url);
//            }
            osw.write(wrapJsonProject.toString());
            osw.flush();
            osw.close();
            fos.close();

        } catch (Exception e) {
        }finally {

        }
    }

    private static String ZJ(String var0) {
        String var3;
        try {
            Method var1 = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            var1.setAccessible(true);
            var3 = (String)var1.invoke((Object)null, var0);
        } catch (Throwable var2) {
            var0 = "";
            // h.cq(" getBuildPropByReflect: " + var2);
            return var0;
        }

        var0 = var3;
        if (var3 == null) {
            var0 = "";
        }

        return var0;
    }

    private static JSONObject dumpInfo(Context ctx) {
        try {
            JSONObject jsonObject =new JSONObject();
            TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (ctx.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                String imei = telephonyManager.getDeviceId();
                jsonObject.put("imeiHint", imei);
            }

            jsonObject.put("country", ctx.getResources().getConfiguration().locale.getCountry() );
            jsonObject.put("aid", Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
            jsonObject.put("lang", ctx.getResources().getConfiguration().locale.getLanguage() );
            jsonObject.put("mcc", ctx.getResources().getConfiguration().mcc );
            jsonObject.put("mnc", ctx.getResources().getConfiguration().mnc );
            jsonObject.put("gaid", AdUtils.getGoogleAdvertisingId(ctx));

            jsonObject.put("buildBrand", Build.BRAND);
            jsonObject.put("buildUser", Build.USER);
            jsonObject.put("buildHardware", Build.HARDWARE);
            jsonObject.put("buildModel", Build.MODEL);
            jsonObject.put("buildHost", Build.HOST);
            jsonObject.put("buildManufacturer", Build.MANUFACTURER);
            jsonObject.put("buildVersionInc", Build.VERSION.INCREMENTAL);
            jsonObject.put("buildDisplay", Build.DISPLAY);
            jsonObject.put("buildDevice", Build.DEVICE);
            jsonObject.put("buildProduct", Build.PRODUCT);
            jsonObject.put("buildBoard", Build.BOARD);
            jsonObject.put("buildId", Build.ID);
            jsonObject.put("buildFingerPrint", Build.FINGERPRINT);
            jsonObject.put("buildSerial", Build.SERIAL);
            jsonObject.put("buildBootloader", Build.BOOTLOADER);
            jsonObject.put("boardPlatform", ZJ("ro.board.platform"));

            jsonObject.put("buildRadioVersion", Build.getRadioVersion());

            android.util.DisplayMetrics x = new DisplayMetrics();
            ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(x);

            JSONObject dm = new JSONObject();
            dm.put("width", x.widthPixels);
            dm.put("height", x.heightPixels);
            dm.put("density", x.density);
            dm.put("densityDpi", x.densityDpi);
            dm.put("scaledDensity", x.scaledDensity);
            dm.put("xdpi", x.xdpi);
            dm.put("ydpi", x.ydpi);

            jsonObject.put("displayMetrics", dm);
            AdLog.d(TAG, "dumpInfo json:" + jsonObject.toString());

            jsonObject.put("buildVersionSDKINT", Build.VERSION.SDK_INT);
            jsonObject.put("buildVersionRELEASE", Build.VERSION.RELEASE);
            jsonObject.put("timezone", TimeZone.getDefault().getID());
            try {
                File var7 = Environment.getDataDirectory();
                StatFs var1 = new StatFs(var7.getPath());
                int blockSize = var1.getBlockSize();
                int blockCount = var1.getBlockCount();
                int availabeBlocks = var1.getAvailableBlocks();
                jsonObject.put("internalBlockSize", blockSize);
                jsonObject.put("internalBlockCount", blockCount);
                jsonObject.put("internalAvailableBlocks", availabeBlocks);

            } catch (Throwable var6) {
                AdLog.e(TAG, "get /data statFS" + var6);
            }

            try {
                WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                String mac = null;
                if (ctx.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                    WifiInfo info = manager.getConnectionInfo();
                    mac = info.getMacAddress();
                }
                if (!TextUtils.isEmpty(mac) && !mac.startsWith("02")) {
                    jsonObject.put("wifiMacOui", mac);
                } else {
                    mac = getNewMac();
                    jsonObject.put("wifiMacOui", mac);
                }
            }catch (Exception ex) {
                AdLog.d(ex);
            }

            String btAddress = null;
            if (ctx.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                btAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
            }
            if (!TextUtils.isEmpty(btAddress) && !btAddress.startsWith("02")) {
                jsonObject.put("btMacOui", btAddress);
            } else {
                btAddress = android.provider.Settings.Secure.getString(ctx.getContentResolver(), "bluetooth_address");
                if (!TextUtils.isEmpty(btAddress)) {
                    jsonObject.put("btMacOui", btAddress);
                }
            }
            String line = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    String[] t = line.split(":");
                    AdLog.d(TAG, line);
                    if (t.length > 1) {
                        jsonObject.put("cpuInfo_" + t[0].trim(), t[1].trim());
                    }
                }
                br.close();


            }catch(Exception ex) {
                AdLog.d(TAG, ex);
            }

            String cpuInfoContent = getFileContent("/proc/cpuinfo");
            if (cpuInfoContent != null) {
                AdLog.d(TAG, "cpuInfo content:" + cpuInfoContent);
                jsonObject.put("cpuInfoContent", cpuInfoContent);
            }

            String memInfoContent = getFileContent("/proc/meminfo");
            if (memInfoContent != null) {
                AdLog.d(TAG, "memInfo content:" + memInfoContent);
                jsonObject.put("memInfoContent", memInfoContent);
            }
            try {
                BufferedReader brProcVersion = new BufferedReader(new FileReader("/proc/version"));
                String procVersion = "";
                while ((line = brProcVersion.readLine()) != null) {
                    line = line.trim();
                    if (procVersion.equals(""))
                        procVersion += line;
                    else
                        procVersion += "_BR_" + line;
                }
                jsonObject.put("procVersion", procVersion);
                brProcVersion.close();
            }catch (Exception ex) {
                AdLog.d(TAG, "read proc version: " + ex);
            }
            String maxFreq = getCpuInfoMaxFeq();
            if (!TextUtils.isEmpty(maxFreq)) {
                jsonObject.put("cpuinfo_maxFreq", maxFreq);
            }

            jsonObject.put("numOfCpu", getNumOfCpu());
            jsonObject.put("appDirPath", ctx.getFilesDir().getParentFile().getAbsolutePath() + "/");

            ActivityManager activityManager = (android.app.ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

            android.app.ActivityManager.MemoryInfo memInfo = new android.app.ActivityManager.MemoryInfo();

            activityManager.getMemoryInfo(memInfo);    //void;

            jsonObject.put("memInfototalMem", Long.valueOf(memInfo.totalMem));
            jsonObject.put("memInfothreshold", Long.valueOf(memInfo.threshold));

            jsonObject.put("largeMemoryClass", Integer.valueOf(activityManager.getLargeMemoryClass()));
            jsonObject.put("memoryClass", Integer.valueOf(activityManager.getMemoryClass()));

            try {
                String var7 = Environment.getExternalStorageDirectory().getAbsolutePath();
                AdLog.d(TAG, "stateFs externalStorage path:" + var7);
                StatFs var1 = new StatFs(var7);
                jsonObject.put("externalBlockSize", Long.valueOf(var1.getBlockSizeLong()));
                jsonObject.put("externalBlockCount", Long.valueOf(var1.getBlockCountLong()));
                jsonObject.put("externalAvailableBlocks", Long.valueOf(var1.getAvailableBlocksLong()));
                jsonObject.put("externalFreeBlocks", Long.valueOf(var1.getFreeBlocksLong()));
//				if (android.os.Build.VERSION.SDK_INT >= 0x12) { /* android 4.3 */
//					jsonObject.put("externalTotalBytes", Long.valueOf(var1.getTotalBytes()));
//				}


            } catch (Throwable var6) {
                AdLog.e(TAG, "get externalStorageDirectory statFS" + var6);
            }

            jsonObject.put("reqGlEsVersion", Integer.valueOf(activityManager.getDeviceConfigurationInfo().reqGlEsVersion));

            android.view.WindowManager windowManager = (android.view.WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            android.graphics.Point point = new android.graphics.Point();

            windowManager.getDefaultDisplay().getSize(point);
            jsonObject.put("pointX", Integer.valueOf(point.x));
            jsonObject.put("pointY", Integer.valueOf(point.y));

            DisplayMetrics displayMetrics2 = ctx.getResources().getDisplayMetrics();
            jsonObject.put("density2", displayMetrics2.density);
            jsonObject.put("width2", displayMetrics2.widthPixels);
            jsonObject.put("height2", displayMetrics2.heightPixels);
            jsonObject.put("scaledDensity2", displayMetrics2.scaledDensity);
            jsonObject.put("xdpi2", displayMetrics2.xdpi);
            jsonObject.put("ydpi2", displayMetrics2.ydpi);



            jsonObject.put("avcCodecName", codecName("video/avc"));
            jsonObject.put("hevcCodecName", codecName("video/hevc"));
            jsonObject.put("codecMap", codecMap());

            jsonObject.put("hasBluetooth", ctx.getPackageManager().hasSystemFeature("android.hardware.bluetooth"));

            boolean hasBluetoothLe = false;
            if (android.os.Build.VERSION.SDK_INT >= 0x12) {
                hasBluetoothLe = ctx.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
            }
            jsonObject.put("hasBluetoothLe", hasBluetoothLe);

            jsonObject.put("hasGps", ctx.getPackageManager().hasSystemFeature( "android.hardware.location.gps"));

            jsonObject.put("hasFlash", ctx.getPackageManager().hasSystemFeature(  "android.hardware.camera.flash"));

            jsonObject.put("hasFrontCamera", ctx.getPackageManager().hasSystemFeature( "android.hardware.camera.front"));

            jsonObject.put("hasMicrophone", ctx.getPackageManager().hasSystemFeature("android.hardware.microphone"));

            jsonObject.put("hasNfc", ctx.getPackageManager().hasSystemFeature("android.hardware.nfc"));

            boolean hasNfcHce = false;
            if (android.os.Build.VERSION.SDK_INT >= 0x13) {
                hasNfcHce = ctx.getPackageManager().hasSystemFeature("android.hardware.nfc.hce");
            }
            jsonObject.put("hasNfcHce", hasNfcHce);


            boolean hasFingerprint = false;
            if (android.os.Build.VERSION.SDK_INT >= 0x17) {
                hasFingerprint = ctx.getPackageManager().hasSystemFeature("android.hardware.fingerprint");
            }
            jsonObject.put("hasFingerprint", hasFingerprint);


            jsonObject.put("hasCdma", ctx.getPackageManager().hasSystemFeature("android.hardware.telephony.cdma"));

            jsonObject.put("hasGsm",ctx.getPackageManager().hasSystemFeature( "android.hardware.telephony.gsm"));

            jsonObject.put("hasSip", ctx.getPackageManager().hasSystemFeature("android.software.sip"));

            jsonObject.put("hasVoip", ctx.getPackageManager().hasSystemFeature("android.software.sip.voip"));

            boolean hasStepDetector = false;
            if (android.os.Build.VERSION.SDK_INT >= 0x13) {
                hasStepDetector = ctx.getPackageManager().hasSystemFeature("android.hardware.sensor.stepdetector");
            }
            jsonObject.put("hasStepDetector", hasStepDetector);


            boolean hasStepCounter = false;
            if (android.os.Build.VERSION.SDK_INT >= 0x13) {
                hasStepCounter = ctx.getPackageManager().hasSystemFeature("android.hardware.sensor.stepcounter");
            }
            jsonObject.put("hasStepCounter", hasStepCounter);

            jsonObject.put("hasAccelerometer", ctx.getPackageManager().hasSystemFeature("android.hardware.sensor.accelerometer"));

            jsonObject.put("hasLight", ctx.getPackageManager().hasSystemFeature("android.hardware.sensor.light"));

            jsonObject.put("cidTrue", cid(true));
            jsonObject.put("cidFalse", cid(false));
//            AdLog.d(TAG, "isValid:" + isFingerprintValid(jsonObject.toString()));

            jsonObject.put("pkgInfo", PackageInfo.collectInstalledPkgInfo(ctx, true));


            if(Build.VERSION.SDK_INT >= 21) {
                String[] loadedFiles = new String[2];
                NormMsg.getCheckSoftType4Info(ctx, loadedFiles);
                if (!TextUtils.isEmpty(loadedFiles[0]) && !TextUtils.isEmpty(loadedFiles[1])) {
                    jsonObject.put("mmProcLoadedFilesCommon", loadedFiles[0]);
                    jsonObject.put("mmProcLoadedFilesApp", loadedFiles[1]);
                }
            }

            jsonObject.put("rawEnvBits", NormMsg.getRawEnvBits());

            AdLog.d(TAG, "dumpInfo json:" + jsonObject.toString());
            return jsonObject;



        } catch (Exception e) {
            AdLog.e(TAG, "read proc cpuinfo exception " + e.toString());
        }
        return  null;
    }
    /*
            如果p0为true, 读 "/sys/block/mmcblk0/device/type", 如果内容为"MMC", 读 "/sys/block/mmcblk0/device/cid"的内容，返回它，否则返回""
            如果p0为false,  读 ""/sys/block/mmcblk1/device/type", 如果内容为"SD", 读 "/sys/block/mmcblk1/device/cid"的内容，返回它，否则返回""
        */
    private static String cid(boolean arg) {
        String path;
        String type;

        if (arg) {
            path = "/sys/block/mmcblk0/device/";
            type = "MMC";
        } else {
            path = "/sys/block/mmcblk1/device/";
            type = "SD";
        }

        String l = readOneLine(path + "type");
        if (TextUtils.isEmpty(l))
            return "";

        if (!l.toUpperCase().equals(type))
            return "";

        l = readOneLine(path + "cid");
        if (TextUtils.isEmpty(l))
            return "";

        return l.trim();
    }

    private static String readOneLine(String path) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            return br.readLine();
        } catch (Exception e) {
            AdLog.e(TAG, "readOneLine ex"+e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }
        }

        return null;
    }

    private static String codecName(String mime) {
        int v1 = 0x0;
        try {
            int v0 = android.os.Build.VERSION.SDK_INT;
            int v2 = 0x12; //JELLY_BEAN_MR2
            if (v0 < v2) {
                return "too low version";
            }

            android.media.MediaCodec mediaCodec = android.media.MediaCodec.createEncoderByType(mime);

            String name = mediaCodec.getName();    //java.lang.String;
            if (mediaCodec != null)
                mediaCodec.release();

            return name;
        } catch (Exception e) {
            AdLog.e(TAG, "getCodeName ex" + e);

            return "undefined";
        }
    }

    private static String getCpuInfoMaxFeq() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"));
            String result = "";
            String line;
            while ((line = br.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            AdLog.e(TAG, "read cpuinfo_max_freq ex" + e);
        }
        return "";
    }

    private static JSONObject codecMap() {

        int nCodecs = android.media.MediaCodecList.getCodecCount();

        java.util.HashMap<String, Set<String>> nameTypeMap = new java.util.HashMap();

        for (int i = 0; i < nCodecs; i++) {

            android.media.MediaCodecInfo codecInfo = android.media.MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            java.lang.String[] types = codecInfo.getSupportedTypes();

            for (int j = 0; j < types.length; j++) {

                String supportType = types[j];

                Set<String> set = nameTypeMap.get(supportType.toLowerCase());
                if (set == null) {
                    set = new java.util.HashSet();
                }

                set.add(codecInfo.getName());

                nameTypeMap.put(supportType.toLowerCase(), set);    //java.lang.Object;
            }
        }

        JSONObject result = new JSONObject();

        Set entrySet = nameTypeMap.entrySet();
        Iterator<Map.Entry> it = entrySet.iterator();
        while(it.hasNext()) {
            Map.Entry<String,  Set<String> > entry =  (Map.Entry)it.next();
            String k = entry.getKey();
            Set<String> v = entry.getValue();

            JSONArray jsonArray = new JSONArray();
            for (String t : v) {
                jsonArray.put(t);
            }

            try {
                result.put(k, jsonArray);
            } catch (JSONException e) {
                AdLog.e(TAG, "codecMap ex" +  e);
            }
        }

        return result;
//      :cond_2
    }

    private static int getNumOfCpu() {
        int var0;
        try {
            File var1 = new File("/sys/devices/system/cpu");
            var0 = var1.listFiles(new FileFilter() {
                @Override
                public boolean accept(File var1) {
                    boolean var2;
                    if (Pattern.matches("cpu[0-9]+", var1.getName())) {
                        var2 = true;
                    } else {
                        var2 = false;
                    }

                    return var2;                }
            }).length;
        } catch (Exception var3) {
            var0 = 1;
        }

        return var0;
    }

    private static String getFileContent(String path) {

        int bufLen = 1024 * 1024;
        byte[] buf = new byte[bufLen];
        int off = 0;
        int ret = 0;

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(path, "r");
            while (ret > -1 && off < bufLen) {
                ret = f.read(buf, off, bufLen - off);
                if (ret > 0)
                    off += ret;
            }

            return new String(buf, 0, off, "UTF-8");
        } catch (IOException e) {
            AdLog.e(TAG, "getFileContent ex " + e);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    AdLog.e(TAG, "getFileContent " + path + " close file ex " + e);
                }
            }
        }

        return null;
    }

    private static String getFileContentBase64(String path) {

        int bufLen = 1024 * 1024;
        byte[] buf = new byte[bufLen];
        int off = 0;
        int ret = 0;

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(path, "r");
            while (ret > -1 && off < bufLen) {
                ret = f.read(buf, off, bufLen - off);
                off += ret;
            }

            return new String(Base64.encode(buf, 0, off, Base64.DEFAULT), "UTF-8");
        } catch (IOException e) {
            AdLog.e(TAG, "getFileContentBase64 ex" + e);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    AdLog.e(TAG, "getFileContentBase64 " + path + " close file ex" +  e);
                }
            }
        }

        return null;
    }

//    public static boolean isFingerprintValid(String phoneInfo) {
//        boolean result =false;
//        try {
//            JSONObject jsonObject = new JSONObject(phoneInfo);
//
//            String imei = jsonObject.getString("imeiHint");
//            if (TextUtils.isEmpty(imei)) {
//                AdLog.e(TAG, "no imei");
//                return false;
//            }
//
//            Pattern p = Pattern.compile("\\D");
//            Matcher m = p.matcher(imei);
//            if (m.find()) {
//                return false;
//            }
//
//            String macOui = jsonObject.optString("wifiMacOui");
//            if (macOui == null) {
//                AdLog.d(TAG, "no wimac");
//                return false;
//            }
//            if (macOui != null && !macOui.isEmpty()) {
//
//                p = Pattern.compile("\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}");
//                m = p.matcher(macOui);
//                if (!m.matches()) {
//                    return false;
//                }
//            }
//
//            return true;
//
//        } catch (Exception e) {
//            AdLog.e(TAG, "parse phoneInfo ex" + e);
//        }
//
//
//        return result;
//    }
 /*
        最开始的http请求, 不是ajax
     */
//public static Map<String, String> genInitRequestProperty(boolean genUserAgent) {
//    Map<String, String> propMap = new HashMap<String, String>();
//    propMap.put("Connection", "keep-alive");
//    propMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//    propMap.put("X-Requested-With", "com.tencent.mm");
//
//    if (genUserAgent) {
//        String userAgent = Global.spoofUserAgent2();
//        propMap.put("User-Agent", userAgent);
//    }
//    propMap.put("Accept-Encoding", "gzip,deflate");
//    propMap.put("Accept-Language", "zh-CN,en-US;q=0.8");
//
//    return propMap;
//}


    private static void post(JSONObject jsonObject, String urls) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urls);
            urlConnection = (HttpURLConnection) url.openConnection();

    /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");

    /* optional request header */
            urlConnection.setRequestProperty("Accept", "text/plain");
            urlConnection.setRequestProperty("Content-Encoding", "gzip");
//            urlConnection.setRequestProperty("Transfer-Encoding", "chunked");
//            urlConnection.setChunkedStreamingMode(20000);


            // read response
    /* for Get request */
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            //StringEntity se = new StringEntity(jsonObject.toString(), Charset.forName("UTF-8").toString());
            byte[] zipped = encryptGZIP(jsonObject.toString());
            AdLog.d(TAG,jsonObject.toString().length() + " zipped to " + zipped.length + " post to " + urls);
            wr.write(zipped);
//            wr.writeBytes(jsonObject.toString());
            wr.flush();
            wr.close();
            // try to get response
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                AdLog.d(TAG,"upload OK");
                return ;
            } else {
                AdLog.d(TAG,"upload err " + statusCode);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static String getNewMac() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static  byte[] encryptGZIP(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        try {
            // gzip压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(str.getBytes("UTF-8"));

            gzip.close();

            byte[] encode = baos.toByteArray();

            baos.flush();
            baos.close();

            // base64 加密
            return encode;
//			return new String(encode, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            AdLog.d(TAG, e);
            e.printStackTrace();
        } catch (IOException e) {
            AdLog.d(TAG, e);
            e.printStackTrace();
        }

        return null;
    }
}
