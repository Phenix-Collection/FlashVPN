package winterfell.flash.vpn.utils;

import android.content.Context;
import android.util.Base64;

import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import winterfell.flash.vpn.R;

public class Crypto {
    private static String keyStr;

    public Crypto() {
        super();
    }

    private static String generateKey(Context context) {
        return context.getString(R.string.key_string);
    }

    public static String d(Context context, String arg1) {
        if(keyStr == null) {
            keyStr = generateKey(context);
        }
        return Crypto.d(arg1, keyStr);
    }

    public static String d(String arg1, String arg2) {
        String v0_1;
        try {
            v0_1 = Crypto.decryptStrAndFromBase64(arg2, arg1);
        }
        catch(Exception v0) {
            v0.printStackTrace();
            v0_1 = "";
        }

        return v0_1;
    }

    private static byte[] decrypt(String arg3, String arg4, byte[] arg5) {
        try {
            MessageDigest v0 = MessageDigest.getInstance("MD5");
            v0.update(arg3.getBytes());
            byte[] v0_1 = v0.digest();
            MessageDigest v1 = MessageDigest.getInstance("SHA-256");
            v1.update(arg4.getBytes());
            return Crypto.decrypt(v0_1, v1.digest(), arg5);
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static byte[] decrypt(byte[] arg4, byte[] arg5, byte[] arg6) {
        try {
            IvParameterSpec v0 = new IvParameterSpec(arg4);
            SecretKeySpec v1 = new SecretKeySpec(arg5, "AES");
            Cipher v2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            v2.init(2, ((Key) v1), ((AlgorithmParameterSpec) v0));
            return v2.doFinal(arg6);
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String decryptStrAndFromBase64(String arg3, String arg4) {
        try {
            return new String(Crypto.decrypt(arg3, arg3, Base64.decode(arg4.getBytes("UTF-8"), 0)), "UTF-8");
        }catch (Exception ex) {
            return "";
        }
    }

    public static String e(Context context, String arg1) {
        if(keyStr == null) {
            keyStr = generateKey(context);
        }
        return Crypto.e(arg1, keyStr);
    }

    public static String e(String arg1, String arg2) {
        String v0_1;
        try {
            v0_1 = Crypto.encryptStrAndToBase64(arg2, arg1);
        }
        catch(Exception v0) {
            v0.printStackTrace();
            v0_1 = "";
        }

        return v0_1;
    }

    private static byte[] encrypt(String arg3, String arg4, byte[] arg5) {
        try {
            MessageDigest v0 = MessageDigest.getInstance("MD5");
            v0.update(arg3.getBytes());
            byte[] v0_1 = v0.digest();
            MessageDigest v1 = MessageDigest.getInstance("SHA-256");
            v1.update(arg4.getBytes());
            return Crypto.encrypt(v0_1, v1.digest(), arg5);
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static byte[] encrypt(byte[] arg4, byte[] arg5, byte[] arg6) {
        try {
            IvParameterSpec v0 = new IvParameterSpec(arg4);
            SecretKeySpec v1 = new SecretKeySpec(arg5, "AES");
            Cipher v2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            v2.init(1, ((Key) v1), ((AlgorithmParameterSpec) v0));
            return v2.doFinal(arg6);
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String encryptStrAndToBase64(String arg3, String arg4) {
        try {
            return new String(Base64.encode(Crypto.encrypt(arg3, arg3, arg4.getBytes("UTF-8")), 0), "UTF-8");
        }catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}

