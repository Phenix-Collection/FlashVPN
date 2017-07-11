package com.polestar.multiaccount.utils;

/**
 * Created by guojia on 2017/1/1.
 */


import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.widgets.locker.LockPatternView;

import java.math.BigInteger;
import java.util.List;

/**
 * Utilities for the lock pattern and its settings.
 *
 * 1) Pattern: 手勢密碼
 * 1) Pattern Password: Pattern 轉為數字後的手勢密碼
 * 2) Encoded Pattern Password: XOR 加密過後的可逆密碼
 * 3) Short Encoded Pattern Password: 取 Encoded Pattern Password 由右而左數起第 1, 3, 5, 7 位組成的 4 位數字串, 不足 4 位數者, 在字串左邊補 '0'.
 *    (ex1: Encoded Pattern Password = 1234567890 -> Shore Encoded Pattern Password = 4680)
 *    (ex2: Encoded Pattern Password = 123 -> Shore Encoded Pattern Password = 0013)
 *
 * A) 本地端與服務器端儲存 Encoded Pattern Password.
 * B) 服務器端提供 Short Encoded Pattern Password 給使用者發送短信
 */
public class LockPatternUtils {
    private static final String LOG_TAG = "LockPattern";

    private static String tempKey = null;
    /**
     * 將手勢轉為 Pattern Password
     */
    public static String patternToPatternPassword(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        String res = "";
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res += String.valueOf(cell.getRow() * 3 + cell.getColumn());
        }
        return res;
    }

    /**
     * 將 Pattern Password 轉為 Encoded Pattern Password
     */
    private static String patternPasswordToEncodedPatternPassword(String patternPassword) {
        if (TextUtils.isEmpty(patternPassword)) {
            return "";
        }

        return encrypt(patternPassword);
    }

    /**
     * 儲存手勢密碼
     */
    public static void setEncodedPatternPassword(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return;
        }

        if (pattern.size() == 0) {
            return;
        }

        String patternPassword = patternToPatternPassword(pattern);
        String encodedPatternPassword = patternPasswordToEncodedPatternPassword(patternPassword);

        LockPatternPref.getIns().setEncodedPatternPassword(encodedPatternPassword);

        AppManager.reloadLockerSetting();
    }

//    /**
//     * 檢查是否設置手勢密碼
//     */
//    public static boolean hasEncodedPatternPassword() {
//    	return !TextUtils.isEmpty(LockPatternPref.getIns().getEncodedPatternPassword());
//    }

    /**
     * 檢查手勢密碼是否正確
     */
    public static boolean isPatternMatched(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return false;
        }

        if (pattern.size() == 0) {
            return false;
        }

        String savedEncodedPatternPassword = tempKey == null? LockPatternPref.getIns().getEncodedPatternPassword() : tempKey;
        if (TextUtils.isEmpty(savedEncodedPatternPassword)) {
            return false;
        }
        String patternPassword = patternToPatternPassword(pattern);
        String encodedPatternPassword = patternPasswordToEncodedPatternPassword(patternPassword);

        return savedEncodedPatternPassword.equals(encodedPatternPassword);
    }

    private static class LockPatternPref {

        private static LockPatternPref mExtGlobalPref;

        public static LockPatternPref getIns() {
            if (mExtGlobalPref == null) {
                mExtGlobalPref = new LockPatternPref();
            }

            return mExtGlobalPref;
        }

        public String getEncodedPatternPassword() {
            return PreferencesUtils.getEncodedPatternPassword(MApp.getApp());
        }

        public void setEncodedPatternPassword(String encodedPwd) {
            PreferencesUtils.setEncodedPatternPassword(MApp.getApp(),encodedPwd);
        }

    }

    // ------------ 加解密核心 -------------------
    private static final String SEED = "011100100010101001110101110110"; // 修改此值必須保證與服務器設定一致 (length = 30)
    private static final int RADIX = 16; // 修改此值必須保證與服務器設定一致

    public static final String encrypt(String password) {
        if (password == null)
            return "";
        if (password.length() == 0)
            return "";

        BigInteger bi_passwd = new BigInteger(password.getBytes());

        BigInteger bi_r0 = new BigInteger(SEED);
        BigInteger bi_r1 = bi_r0.xor(bi_passwd);

        return bi_r1.toString(RADIX);
    }

    public static int getAspectSquareFillModePatternWidth(int maxWidth, int maxHeight, int paddingBottom) {
        int width = Math.min(maxWidth, maxHeight - paddingBottom);
        return width;
    }

    public static int getAspectSquareFillModePatternHeight(int patternWidth, int paddingBottom) {
        return patternWidth + paddingBottom;
    }

    public static void setTempKey(String key) {
        tempKey = key;
    }

    public static String getTempKey() {
        return tempKey;
    }
}
