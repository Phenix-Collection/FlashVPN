package com.polestar.grey;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.polestar.ad.AdLog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import nativesdk.ad.common.utils.DeviceUtil;

/**
 * Created by guojia on 2018/3/17.
 */

public class Fingerprint {

    private final static String FINGERPRINT_FILE = "fingerprint";

    public static void genFingerprint(Context ctx, String url) {
        File file = new File(ctx.getFilesDir(),FINGERPRINT_FILE);
        if (file.exists()) {
            AdLog.d("fingerprint exist");
            return ;
        }

        try {
            file.createNewFile();
            FileOutputStream fos=new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos);
            JSONObject jsonObject =new JSONObject();
            TelephonyManager telephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();


            //jsonObject.put("imeiHint", imei.substring(0, 8));
            jsonObject.put("ver", "1" );
            jsonObject.put("country", ctx.getResources().getConfiguration().locale.getCountry() );
            jsonObject.put("aid", Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID));
            jsonObject.put("lang", ctx.getResources().getConfiguration().locale.getLanguage() );
            jsonObject.put("mcc", ctx.getResources().getConfiguration().mcc );
            jsonObject.put("mnc", ctx.getResources().getConfiguration().mnc );
            jsonObject.put("gpid", DeviceUtil.getGoogleAdvertisingId(ctx));
            jsonObject.put("imei", imei);
            jsonObject.put("BBrand", Build.BRAND);
            jsonObject.put("BUser", Build.USER);
            jsonObject.put("BHw", Build.HARDWARE);
            jsonObject.put("BModel", Build.MODEL);
            jsonObject.put("BHost", Build.HOST);
            jsonObject.put("BManu", Build.MANUFACTURER);
            jsonObject.put("BVerInc", Build.VERSION.INCREMENTAL);
            jsonObject.put("BDisplay", Build.DISPLAY);
            jsonObject.put("BDev", Build.DEVICE);
            jsonObject.put("BProd", Build.PRODUCT);
            jsonObject.put("BBoard", Build.BOARD);
            jsonObject.put("BId", Build.ID);
            jsonObject.put("BFingerPrint", Build.FINGERPRINT);
            jsonObject.put("BSerial", Build.SERIAL);
            jsonObject.put("Bloader", Build.BOOTLOADER);

            jsonObject.put("BRadioVer", Build.getRadioVersion());

            android.util.DisplayMetrics x = new DisplayMetrics();
            ((WindowManager)ctx.getSystemService(ctx.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(x);

            JSONObject dm = new JSONObject();
            dm.put("width", x.widthPixels);
            dm.put("height", x.heightPixels);
            dm.put("density", x.density);
            dm.put("densityDpi", x.densityDpi);
            dm.put("scaledDensity", x.scaledDensity);
            dm.put("xdpi", x.xdpi);
            dm.put("ydpi", x.ydpi);

            jsonObject.put("displayMetrics", dm);

            jsonObject.put("BVerSDKINT", Build.VERSION.SDK_INT);
            jsonObject.put("BVerRELEASE", Build.VERSION.RELEASE);
            jsonObject.put("timezone", TimeZone.getDefault().getID());
            try {
                File var7 = Environment.getDataDirectory();
                StatFs var1 = new StatFs(var7.getPath());
                int blockSize = var1.getBlockSize();
                int blockCount = var1.getBlockCount();
                int availabeBlocks = var1.getAvailableBlocks();
                jsonObject.put("intBlockSize", blockSize);
                jsonObject.put("intBlockCnt", blockCount);
                jsonObject.put("intAvaiBlocks", availabeBlocks);

            } catch (Throwable var6) {
            }


            try {
                WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                if (manager != null) {
                    WifiInfo info = manager.getConnectionInfo();
                    if (info != null) {
                        String mac = info.getMacAddress();
                        if (!TextUtils.isEmpty(mac) && !mac.startsWith("02")) {
                            //jsonObject.put("wifiMacOui", mac.substring(0, 8).toLowerCase());
                            jsonObject.put("wimac", mac);
                        } else {
                            mac = getNewMac();
                            jsonObject.put("wimac", mac);
                        }
                    }
                }

                String btAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
                if (!TextUtils.isEmpty(btAddress) && !btAddress.startsWith("02")) {
                    //jsonObject.put("btMacOui", btAddress.substring(0, 8).toLowerCase());
                    jsonObject.put("btmac", btAddress);
                } else {
                    btAddress = android.provider.Settings.Secure.getString(ctx.getContentResolver(), "bluetooth_address");
                    jsonObject.put("btmac", btAddress);
                }
            }catch (Exception ex) {
                AdLog.d(ex);
            }


            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line = "";
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] t = line.split(":");
                if (t.length > 1) {
                    jsonObject.put("cpuInfo_" + t[0].trim(), t[1].trim());
                }

            }
            AdLog.d(jsonObject.toString());
            post(jsonObject, url);
            br.close();
            osw.write(jsonObject.toString());
            osw.flush();
            osw.close();
            fos.close();

        } catch (Exception e) {
        }finally {

        }
    }

    private static void post(JSONObject jsonObject, String urls) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urls);
            urlConnection = (HttpURLConnection) url.openConnection();

    /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

    /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");

            // read response
    /* for Get request */
           AdLog.d("post to " + urls);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(jsonObject.toString());
            wr.flush();
            wr.close();
            // try to get response
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                AdLog.d("upload OK");
                return ;
            } else {
                AdLog.d("upload err " + statusCode);
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
