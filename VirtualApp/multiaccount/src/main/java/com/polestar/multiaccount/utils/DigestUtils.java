package com.polestar.multiaccount.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class DigestUtils {
    private static final String UTF8 = "UTF-8";

    private static final IvParameterSpec IV = new IvParameterSpec(new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();


    private static final String ALGORITHM = "AES";

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private static byte[] getHash(final String algorithm, final String text) {
        try {
            return getHash(algorithm, text.getBytes(UTF8));
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static byte[] getHash(final String algorithm, final byte[] data) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(data);
            return digest.digest();
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static String encrypt(final String str, String key) {
        try {
        	if (TextUtils.isEmpty(str)){
        		return "";
        	}
            return encrypt(str.getBytes(UTF8), key);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static final byte[] md5(byte[] data) {
        final byte[] md5 = getHash("MD5", data);
        return md5;
    }

    public static final String md5Hex(String data) {
        final byte[] md5 = getHash("MD5", data.getBytes());
        return bytesToHex(md5);
    }

    public static final String md5Hex(byte[] data) {
        final byte[] md5 = getHash("MD5", data);
        return bytesToHex(md5);
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @SuppressLint("TrulyRandom")
    private static String encrypt(final byte[] data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            Key secretKey = new SecretKeySpec(getHash("MD5", key), ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV);
            final byte[] encryptData = cipher.doFinal(data);

            final byte[] base64En = Base64.encode(encryptData, Base64.NO_WRAP);

            return new String(base64En, UTF8);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static String decrypt(final String str, String key) {
        try {
        	if (TextUtils.isEmpty(str)){
        		return "";
        	}
            return decrypt(str.getBytes(UTF8), key);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static String decrypt(final byte[] data, String key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            Key secretKey = new SecretKeySpec(getHash("MD5", key), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IV);
            final byte[] base64De = Base64.decode(data, Base64.NO_WRAP);
            final byte[] decryptData = cipher.doFinal(base64De);

            return new String(decryptData, UTF8);
        } catch (final Exception ex) {
//            throw new RuntimeException(ex.getMessage());
        	ex.printStackTrace();
        	return "";
        }
    }
}
