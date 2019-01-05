package com.polestar.grey;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Created by eepaul on 03/07/2018.
 */

public class PackageInfo {
    private static final String TAG = "PackageInfo";

    public String pkgName;
    public long fileSize;
    public int isSysApp;  //1: 系统应用 0: 普通应用
    public int versionCode;
    public String versionName;
    public String applicationLabel;
    public String sig;    //appInfo signatures --> X509 --> md5 --> hex string
    public String sourceDir;

    public List<byte[]> sigs;
    public String sha1Dig;


    public PackageInfo(String pkg, long fSize, int sysApp, int vCode, String vName, String appLabel, String sig, String sourceDir) {
        pkgName = pkg;
        fileSize = fSize;
        isSysApp = sysApp;
        versionCode = vCode;
        versionName = vName;
        applicationLabel = appLabel;
        this.sig = sig;
        this.sourceDir = sourceDir;
    }

    public PackageInfo(String pkg, long fSize, int sysApp, int vCode, String vName, String appLabel, String sig, String sourceDir, String json2DArray, String sha1) {
        pkgName = pkg;
        fileSize = fSize;
        isSysApp = sysApp;
        versionCode = vCode;
        versionName = vName;
        applicationLabel = appLabel;
        this.sig = sig;
        this.sourceDir = sourceDir;

        if ("None".equals(sha1))
            sha1Dig = null;
        else
            sha1Dig = sha1;

        try {
            JSONArray jsonArray = new JSONArray(json2DArray);
            sigs = new ArrayList<byte []>(jsonArray.length());
            for (int i = 0;i < jsonArray.length(); i++) {
                JSONArray byteArray = jsonArray.getJSONArray(i);
                byte[] bytes = new byte[byteArray.length()];
                for (int j = 0; j < byteArray.length(); j++) {
                    bytes[j] = (byte)byteArray.getInt(j);
                }

                sigs.add(bytes);
            }
        } catch (Exception e) {
            Log.e(TAG, "RiskScannerPackageInfo constructor ex", e);
        }

    }

    public PackageInfo(Context context, android.content.pm.PackageInfo pkgInfo) {
        pkgName = pkgInfo.packageName;
        fileSize = new File(pkgInfo.applicationInfo.sourceDir).length();
        isSysApp = ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? 1 : 0;
        versionCode = pkgInfo.versionCode;
        versionName = pkgInfo.versionName;
        applicationLabel = pkgInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();

        sig = null;
        sigs = new ArrayList<>();
        try {
            CertificateFactory v3 = CertificateFactory.getInstance("X.509");
            Signature signature = pkgInfo.signatures[0];
            Certificate v0 = a(v3, signature);
            byte[] en = v0.getEncoded();
            en = bC(en);

            sigs.add(en);
            if (en != null && en.length > 0) {
                String hex = "";
                for (byte b : en) {
                    hex += String.format("%02x", b);
                }
                sig = hex.toUpperCase();
            }
        } catch (Exception e) {
            Log.e(TAG, "PackageInfo constructor ex", e);
        }


        sourceDir = pkgInfo.applicationInfo.sourceDir;
        sha1Dig = ZI(sourceDir);

    }

    private static String ZI(String p0) {
        if (!new File(p0).exists())
            return null;

        java.util.zip.ZipFile zipFile = null;
        BufferedReader br = null;
        try {
            zipFile = new java.util.zip.ZipFile(p0);
            ZipEntry manifestEntry = zipFile.getEntry("META-INF/MANIFEST.MF");

            long v4 = manifestEntry.getSize();
            if (v4 >= 0xa00000) {
                return null;
            }

            br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(manifestEntry)));
            String l;
            while (true) {
                l = br.readLine();
                if (l == null)
                    break;

                if (!l.contains("classes.dex")) {
                    continue;
                }

                l = br.readLine();
                if (l == null)
                    break;

                if (!l.contains("SHA1-Digest")) {
                    continue;
                }

                int colonIdx = l.indexOf(":");
                if (colonIdx <=0) {
                    continue;
                }

                return l.substring(colonIdx+1).trim();
            }

        } catch (Exception e) {
            Log.e(TAG, "ZI ex", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }

            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }

        return null;

    }

    public static byte[] bC(byte[] var0) {
        Object var1 = null;

        try {
            MessageDigest var2 = MessageDigest.getInstance("MD5");
            var2.update(var0);
            var0 = var2.digest();
        } catch (NoSuchAlgorithmException var3) {
            var0 = (byte[])var1;
        } catch (Exception var4) {
            var0 = (byte[])var1;
        }

        return var0;
    }

    private static Certificate a(CertificateFactory var0, Signature var1) {
        ByteArrayInputStream var11 = new ByteArrayInputStream(var1.toByteArray());

        X509Certificate var10;
        try {
            var10 = (X509Certificate)var0.generateCertificate(var11);
            return var10;
        } catch (Throwable var8) {
            ;
        } finally {
            try {
                var11.close();
            } catch (IOException var7) {
                ;
            }

        }

        var10 = null;
        return var10;
    }

    public JSONObject toJsonObject() {
        JSONObject result = new JSONObject();
        try {
            result.put("pkgName", pkgName);
            result.put("fileSize", fileSize);
            result.put("isSysApp", isSysApp);
            result.put("versionCode", versionCode);
            result.put("versionName", versionName);
            result.put("applicationLabel", applicationLabel);
            result.put("sig", sig);
            result.put("sourceDir", sourceDir);

            JSONArray sigsArray = new JSONArray();
            for (byte[] sig : sigs) {
                JSONArray singleSigArray = new JSONArray();
                for (byte b : sig) {
                    singleSigArray.put((int)b);
                }

                sigsArray.put(singleSigArray);
            }
            result.put("sigs", sigsArray);
            result.put("sha1Dig", sha1Dig);

            return result;
        } catch (Exception e) {
            Log.e(TAG, "toJsonObject ex", e);
        }

        return null;
    }

    private static byte[] jsonArray2ByteArray(JSONArray jsonArray) throws JSONException {
        byte[] result = new byte[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            result[i] = (byte) jsonArray.getInt(i);
        }

        return result;
    }


    public static PackageInfo fromJsonObject(JSONObject jsonObject) {
        try {
            String pkgName = jsonObject.getString("pkgName");
            long fileSize = jsonObject.getLong("fileSize");
            int isSysApp = jsonObject.getInt("isSysApp");
            int versionCode = jsonObject.getInt("versionCode");
            String versionName = jsonObject.getString("versionName");
            String label = jsonObject.getString("applicationLabel");
            String sig = jsonObject.getString("sig");
            String sourceDir = jsonObject.getString("sourceDir");

            PackageInfo info = new PackageInfo(pkgName, fileSize, isSysApp, versionCode, versionName, label, sig, sourceDir);
            JSONArray sigsJsonArray = jsonObject.getJSONArray("sigs");
            info.sigs = new ArrayList<>(sigsJsonArray.length());
            for (int i = 0; i < sigsJsonArray.length(); i++) {
                info.sigs.add(jsonArray2ByteArray(sigsJsonArray.getJSONArray(i)));
            }

            info.sha1Dig = jsonObject.optString("sha1Dig", null);

            return info;

        } catch (Exception e) {
            Log.e(TAG, "fromJsonObject ex", e);
        }

        return null;
    }

    public static PackageInfo[] loadPackageInfo(final String jsonStr) {
        try {


            JSONArray jsonArray = new JSONArray(jsonStr);
            PackageInfo [] result = new PackageInfo[jsonArray.length()];

            for (int i =0; i < jsonArray.length(); i++) {
                result[i] = fromJsonObject(jsonArray.getJSONObject(i));
            }

            return result;

        } catch (Exception e) {
            Log.e(TAG, "loadPackageInfo ex", e);
        }

        return null;
    }

    public static PackageInfo[] loadPackageInfo(final File jsonFile) {
        BufferedReader br = null;
        try {
            String jsonStr = "";
            br = new BufferedReader(new FileReader(jsonFile));
            String l = null;
            while ((l = br.readLine()) != null) {
                jsonStr += (l + "\n");
            }


            return loadPackageInfo(jsonStr);

        } catch (Exception e) {
            Log.e(TAG, "loadPackageInfo ex", e);
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


    public static JSONArray collectInstalledPkgInfo(Context context, boolean isSystem) {
        List<android.content.pm.PackageInfo> pkgInfos = context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
        ArrayList<android.content.pm.PackageInfo> filtered = new ArrayList<>();
        for (android.content.pm.PackageInfo pkgInfo : pkgInfos) {
            if (isSystem && (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                filtered.add(pkgInfo);
            } else if (!isSystem && (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filtered.add(pkgInfo);
            }
        }

        JSONArray result = new JSONArray();
        for (android.content.pm.PackageInfo pkgInfo : filtered) {
            PackageInfo myPkgInfo = new PackageInfo(context, pkgInfo);
            Log.d(TAG, "collectInstalledPkgInfo:" + myPkgInfo.toString());
            result.put(myPkgInfo.toJsonObject());
        }

        return result;
    }

    @Override
    public String toString() {
        String sigsStr = "[";
        for (byte[] sig : sigs) {
            String sigStr = "[";
            for (byte b : sig) {
                sigStr += String.format("%d, ", b);
            }
            sigStr += "], ";
            sigsStr += sigStr;
        }
        sigsStr += "]";
        return "PackageInfo{" +
                "pkgName='" + pkgName + '\'' +
                ", fileSize=" + fileSize +
                ", isSysApp=" + isSysApp +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", applicationLabel='" + applicationLabel + '\'' +
                ", sig='" + sig + '\'' +
                ", sourceDir='" + sourceDir + '\'' +
                ", sigs=" + sigsStr +
                ", sha1Dig='" + sha1Dig + '\'' +
                '}';
    }
}
