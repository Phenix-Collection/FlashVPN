package com.polestar.superclone.reward;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.polestar.superclone.component.activity.LauncherActivity;
import com.polestar.superclone.utils.MLogs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

public class AdCipher {
//
//    private static byte[] getCertificateSHA1Fingerprint(Context context) {
//        PackageManager pm = context.getPackageManager();
//        String packageName = context.getPackageName();
//
//        try {
//            PackageInfo packageInfo = pm.getPackageInfo(packageName,
//                    PackageManager.GET_SIGNATURES);
//            Signature[] signatures = packageInfo.signatures;
//            byte[] cert = signatures[0].toByteArray();
//            X509Certificate x509 = X509Certificate.getInstance(cert);
//            MessageDigest md = MessageDigest.getInstance("SHA1");
//            return md.digest(x509.getEncoded());
//        } catch (PackageManager.NameNotFoundException | CertificateEncodingException |
//                NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (javax.security.cert.CertificateException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for ( int j = 0; j < bytes.length; j++ ) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
//
//    public static String getCertificateSHA(Context context) {
//        String s = bytesToHex(getCertificateSHA1Fingerprint(context));
//        Log.d("JJJJJ", s);
//        return s;
//    }
//    private static String CIPHER_NAME = "AES/ECB/PKCS5PADDING";
//    private static int CIPHER_KEY_LEN = 16; //128 bits
//
//    /**
//     * Encrypt data using AES Cipher (CBC) with 128 bit key
//     *
//     *
//     * @param key  - key to use should be 16 bytes long (128 bits)
//     * @param iv - initialization vector
//     * @param data - data to encrypt
//     * @return encryptedData data in base64 encoding with iv attached at end after a :
//     */
//    public static String encrypt(String key, String iv, String data) {
//        try {
//            if (key.length() < AdCipher.CIPHER_KEY_LEN) {
//                int numPad = AdCipher.CIPHER_KEY_LEN - key.length();
//
//                for(int i = 0; i < numPad; i++){
//                    key += "0"; //0 pad to len 16 bytes
//                }
//
//            } else if (key.length() > AdCipher.CIPHER_KEY_LEN) {
//                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
//            }
//
//
//            IvParameterSpec initVector = new IvParameterSpec(iv.getBytes("ISO-8859-1"));
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");
//
//            Cipher cipher = Cipher.getInstance(AdCipher.CIPHER_NAME);
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, initVector);
//
//            byte[] encryptedData = cipher.doFinal((data.getBytes()));
//
//            String base64_EncryptedData = new String(Base64.encodeToString(encryptedData, Base64.DEFAULT));
//            String base64_IV = new String(Base64.encodeToString(iv.getBytes("ISO-8859-1"), Base64.DEFAULT));
//
//            return base64_EncryptedData + ":" + base64_IV;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    public static String encrypt(String key, String data) {
//        try {
//            if (key.length() < AdCipher.CIPHER_KEY_LEN) {
//                int numPad = AdCipher.CIPHER_KEY_LEN - key.length();
//
//                for(int i = 0; i < numPad; i++){
//                    key += "0"; //0 pad to len 16 bytes
//                }
//
//            } else if (key.length() > AdCipher.CIPHER_KEY_LEN) {
//                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
//            }
//
//
//            //IvParameterSpec initVector = new IvParameterSpec(iv.getBytes("ISO-8859-1"));
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");
//
//            Cipher cipher = Cipher.getInstance(AdCipher.CIPHER_NAME);
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//
//            byte[] encryptedData = cipher.doFinal((data.getBytes()));
//
//            String base64_EncryptedData = new String(Base64.encodeToString(encryptedData, Base64.DEFAULT));
//            //String base64_IV = new String(Base64.encodeToString(iv.getBytes("ISO-8859-1"), Base64.DEFAULT));
//
//            return base64_EncryptedData;
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    /**
//     * Decrypt data using AES Cipher (CBC) with 128 bit key
//     *
//     * @param key - key to use should be 16 bytes long (128 bits)
//     * @param data - encrypted data with iv at the end separate by :
//     * @return decrypted data string
//     */
//
//    public static String decrypt(String key, String data) {
//        try {
//
//            if (key.length() < AdCipher.CIPHER_KEY_LEN) {
//                int numPad = AdCipher.CIPHER_KEY_LEN - key.length();
//
//                for(int i = 0; i < numPad; i++){
//                    key += "0"; //0 pad to len 16 bytes
//                }
//
//            } else if (key.length() > AdCipher.CIPHER_KEY_LEN) {
//                key = key.substring(0, CIPHER_KEY_LEN); //truncate to 16 bytes
//            }
//
//            String[] parts = data.split(":");
//
//            IvParameterSpec iv = new IvParameterSpec(Base64.decode(parts[1], Base64.DEFAULT));
//            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("ISO-8859-1"), "AES");
//
//            Cipher cipher = Cipher.getInstance(AdCipher.CIPHER_NAME);
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
//
//            byte[] decodedEncryptedData = Base64.decode(parts[0], Base64.DEFAULT);
//
//            byte[] original = cipher.doFinal(decodedEncryptedData);
//
//            return new String(original);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private static byte[] getCertificateSHA1Fingerprint(Context context) {
//        PackageManager pm = context.getPackageManager();
//        String packageName = context.getPackageName();
//
//        try {
//            PackageInfo packageInfo = pm.getPackageInfo(packageName,
//                    PackageManager.GET_SIGNATURES);
//            Signature[] signatures = packageInfo.signatures;
//            byte[] cert = signatures[0].toByteArray();
//            X509Certificate x509 = X509Certificate.getInstance(cert);
//            MessageDigest md = MessageDigest.getInstance("SHA1");
//            return md.digest(x509.getEncoded());
//        } catch (PackageManager.NameNotFoundException | CertificateEncodingException |
//                NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (javax.security.cert.CertificateException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
//    public static String bytesToHex(byte[] bytes) {
//        char[] hexChars = new char[bytes.length * 2];
//        for ( int j = 0; j < bytes.length; j++ ) {
//            int v = bytes[j] & 0xFF;
//            hexChars[j * 2] = hexArray[v >>> 4];
//            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
//        }
//        return new String(hexChars);
//    }

//    public static int getCertificateHashCode(Context context) {
//        PackageManager pm = context.getPackageManager();
//        String packageName = context.getPackageName();
//
//        try {
//            PackageInfo packageInfo = pm.getPackageInfo(packageName,
//                    PackageManager.GET_SIGNATURES);
//            Signature[] signatures = packageInfo.signatures;
//            Signature signature = signatures[0];
//            return signature.hashCode();
//        } catch (PackageManager.NameNotFoundException  e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }

}
