package com.polestar.multiaccount.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 与后台约定的自定义附加头构建类
 *
 * @author toby.du
 */
public class RequestHeadersBuilder {

    private RequestHeadersBuilder() {
    }

    public static String mDeviceId = "";

    public static String getDeviceId(Context context) {
        if (TextUtils.isEmpty(mDeviceId)) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        }
        return mDeviceId;
    }

    //	private static final String CHARSET_NAME = SBLConstants.DEFAULT_CHARSET;
    public static final String CHARSET_NAME = "UTF8";
    public static final String ACCESS_KEY = "IDPplwaYuywnTT2B"; // server alloc this key for app

    private static final String PREFIX_KEY = "Sdk-";
    private static final String KEY_Ass_apiver = PREFIX_KEY + "apiver";
    private static final String KEY_Ass_appver = PREFIX_KEY + "appver";
    private static final String KEY_Ass_accesskey = PREFIX_KEY + "accesskey";
    private static final String KEY_Ass_time = PREFIX_KEY + "time";
    private static final String KEY_Ass_packagename = PREFIX_KEY + "packagename";
    private static final String KEY_Ass_id = PREFIX_KEY + "deviceid";
    private static final String KEY_Ass_contentmd5 = PREFIX_KEY + "contentmd5";
    private static final String KEY_Ass_signature = PREFIX_KEY + "signature";

    public static Map<String, String> getProtocolHeaders(Context app, String content, String sn, boolean isInit) {
        Map<String, String> result = new HashMap<String, String>();
//		result.put(KEY_Ass_apiver, SBLConstants.KEY_ASS_APIVER);
//		result.put(KEY_Ass_appver, String.valueOf(ApplicationUtils.getInstance(app).getVersionCode()));
        result.put(KEY_Ass_apiver, "2.1");  // should > 2.0
        result.put(KEY_Ass_appver, "1.0");
        result.put(KEY_Ass_accesskey, ACCESS_KEY);

        long now = System.currentTimeMillis();
        result.put(KEY_Ass_time, String.valueOf(now));


//		if (Double.parseDouble(SBLConstants.KEY_ASS_APIVER) >= 0.3) {
//			String packageName = ApplicationUtils.getInstance(app).getPackageName();
        String packageName = app.getPackageName();
        if (!TextUtils.isEmpty(packageName)) {
            result.put(KEY_Ass_packagename, packageName);
        }
//		}

        String id = getDeviceId(app);
        if (!TextUtils.isEmpty(id)) {
            result.put(KEY_Ass_id, id);
        }

        if (content == null) {
            content = "";//服务器对空字符串也做了md5
        }
        String contentmd5 = MD5Utils.md5(content);
        if (contentmd5 != null && contentmd5.length() > 0) {
            result.put(KEY_Ass_contentmd5, contentmd5);
        }

        result = RequestHeadersBuilder.build(result, sn);

        return result;
    }

    private static Map<String, String> build(Map<String, String> map, String sn) {
        Map<String, String> temp = new HashMap<String, String>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (key.startsWith(PREFIX_KEY)) {
                String value = map.get(key);
                temp.put(key, value);
            }
        }
        String encodeString = encodeParameters(temp, sn);
        String signature = MD5Utils.encodeHexStr(encodeString);
        map.put(KEY_Ass_signature, signature);
        return map;
    }

    private static String encodeParameters(Map<String, String> params, String sn) {
        Map<String, String> treeMap = new TreeMap<String, String>(params);
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), CHARSET_NAME));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), CHARSET_NAME));
                encodedParams.append('&');
            }
            if (!TextUtils.isEmpty(sn)) {
                encodedParams.append(sn);
            } else {
                encodedParams.append(JNISecretApi.getJNISecretApi().getSecret(JNISecretApi.SecretType.APPACESS));
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + CHARSET_NAME, uee);
        }
    }
}