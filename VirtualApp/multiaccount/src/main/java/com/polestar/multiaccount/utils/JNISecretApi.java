package com.polestar.multiaccount.utils;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;


public class JNISecretApi {
    private static JNISecretApi mInstance;
    private static String mLock = "mlock";

    public static JNISecretApi getJNISecretApi() {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new JNISecretApi();
            }
        }

        return mInstance;
    }

    public enum SecretType {
        REPORTACCESS, REPORTEN, REPORTDE,//上报用的
        APPACESS, APPEN, APPDE//app用的
    }

    private Map<SecretType, String> mSecrets = new HashMap<SecretType, String>();

    public synchronized String getSecret(SecretType type) {
        String secret = mSecrets.get(type);
        if (TextUtils.isEmpty(secret)) {
            switch (type) {
                case REPORTACCESS:
                    secret = "TLLk59TJmCIMYB8ZoqdjCHYTnawxNXhU";
                    break;
                case REPORTEN:
                    secret = "kRJCSN1RAo6TFOY4FV";
                    break;
                case REPORTDE:
                    secret = "JORsciTuZ0gIEwunX9";
                    break;
                case APPACESS://加密追加串
                    secret = "QLxPOmv3i6jBb70nIoRhRGYlUAhzOJgc";
                    break;
                case APPEN:
                    secret = "WXyneuMVMrhyBya27S";
                    break;
                case APPDE:
                    secret = "rRArT1HzYdLSWyxtwc";
                    break;
                default:
                    secret = "";
                    break;
            }
            mSecrets.put(type, secret);
        }

        return secret;
    }
}
