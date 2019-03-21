package winterfell.flash.vpn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by guojia on 2019/3/21.
 */

public class MD5Utils {
    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 用MD5算法加密字节数组
     *
     * @param bytes
     *            要加密的字节
     * @return byte[] 加密后的字节数组，若加密失败，则返回null
     */
    public static byte[] encode(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] digesta = digest.digest(bytes);
            return digesta;
        } catch (Exception e) {
            return null;
        }
    }

    public static String md5(String input) {
        String result = input;
        if (input != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(input.getBytes(DEFAULT_CHARSET));
                BigInteger hash = new BigInteger(1, md.digest());
                result = hash.toString(16);
                while (result.length() < 32) {
                    result = "0" + result;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    /**
     * 计算文件的md5，文件较短的情况，打文件自己处理
     *
     * @param filePath
     *            文件路径
     * @return md5结果，若加密失败，则返回null
     */
    public static byte[] encodeFile(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            File file = new File(filePath);
            if (!file.exists())
                return null;
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            byte[] digesta = null;
            int readed = -1;
            try {
                while ((readed = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, readed);
                }
                digesta = digest.digest();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return digesta;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * 计算文件的md5,转换成hex String
//     *
//     * @param filePath
//     *            文件路径
//     * @return md5结果，若加密失败，则返回null
//     */
//    public static String encodeFileHexStr(String filePath) {
//        return HexUtils.bytes2HexStr(encodeFile(filePath));
//    }
//
//    public static String encodeHexStr(String value) {
//        if (value == null) {
//            return null;
//        }
//        try {
//            return HexUtils.bytes2HexStr(encode(value.getBytes("UTF-8")));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public static String getStringMd5(String plainText) {
        MessageDigest md = null;
        try {

            md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());

        } catch (Exception e) {
            return null;

        }
        return encodeHex(md.digest());
    }


    public static String encodeHex(byte[] data) {
        if (data == null) {
            return null;
        }

        final String HEXES = "0123456789abcdef";
        int len = data.length;
        StringBuilder hex = new StringBuilder(len * 2);

        for (int i = 0; i < len; ++i) {

            hex.append(HEXES.charAt((data[i] & 0xF0) >>> 4));
            hex.append(HEXES.charAt((data[i] & 0x0F)));
        }

        return hex.toString();
    }
}

