package com.polestar.multiaccount.net;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.model.Feedback;
import com.polestar.multiaccount.utils.DigestUtils;
import com.polestar.multiaccount.utils.JNISecretApi;
import com.polestar.multiaccount.utils.Logs;
import com.polestar.multiaccount.utils.RequestHeadersBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;


/**
 * Created by hxx on 7/22/16.
 */
public class HttpUtil {
    private static final int timeOut = 3 * 1000;

    public static int submitFeedback(Context context, String content, String email) {
        int resultCode = -1;
        String feedbackUrl;
        if (Constants.IS_RELEASE_VERSION) {
            feedbackUrl = "http://api.appclone.info/feedback/report"; //线上地址
        } else {
            feedbackUrl = "http://192.168.40.234:3235/feedback/report"; // 测试地址
        }

        Feedback data = new Feedback();
        data.feedback = new Feedback.FeedbackContent();
        data.feedback.email = email;
        data.feedback.content = content;
        data.feedback.score = "0";
        String json = "";
        Gson gson = new Gson();
        json = gson.toJson(data);

        Logs.d("json:" + json);
        String contentBody = encryptHttpBody(context, json.toString());
        Map<String, String> header = RequestHeadersBuilder.getProtocolHeaders(context, contentBody, null, false);
        if (header == null) {
            return resultCode;
        }

        BufferedReader reader = null;
        try {
            URL url = new URL(feedbackUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setConnectTimeout(timeOut);
            Set<String> keyset = header.keySet();
            for (String key : keyset) {
                urlConn.addRequestProperty(key, header.get(key));
            }
            OutputStream os = urlConn.getOutputStream();
            PrintWriter pw = new PrintWriter(os, true);
            pw.write(contentBody);

            pw.flush();
            pw.close();
            os.close();
            urlConn.connect();

            // get response from server
            String line;
            StringBuffer sb = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            resultCode = urlConn.getResponseCode();
            Logs.d("resultCode: " + resultCode + ", sb: " + sb);

            if (resultCode == HttpURLConnection.HTTP_OK) {
                // decrypt the response result from server
                String jsonstr = new String(sb.toString().getBytes(), RequestHeadersBuilder.CHARSET_NAME);
                jsonstr = DigestUtils.decrypt(jsonstr.trim(),
                        RequestHeadersBuilder.ACCESS_KEY + JNISecretApi.getJNISecretApi().getSecret(JNISecretApi.SecretType.APPDE));
                Logs.d("decrypt jsonstr: " + jsonstr);
                JSONObject ret = new JSONObject(jsonstr);
                resultCode = ret.getInt("code");
                Logs.d("code: " + resultCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultCode;
    }

    public static String getInfoFromUrl(String path) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        String line;
        try {
            URL url = new URL(path);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setConnectTimeout(timeOut);
            urlConn.connect();
            reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    private static String encryptHttpBody(Context context, String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        String id = RequestHeadersBuilder.getDeviceId(context);
        String secretKey = id + JNISecretApi.getJNISecretApi().getSecret(JNISecretApi.SecretType.APPEN);
        json = DigestUtils.encrypt(json, secretKey);

        return json;
    }
}