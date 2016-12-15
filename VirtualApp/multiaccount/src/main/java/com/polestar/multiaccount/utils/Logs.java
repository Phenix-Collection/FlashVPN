package com.polestar.multiaccount.utils;

import android.util.Log;

import com.polestar.multiaccount.BuildConfig;

import java.io.File;

/**
 * Created by yxx on 2016/8/4.
 */
public class Logs {

    public final static boolean DEBUG = BuildConfig.DEBUG || isDebugMode();
    public final static String DEFAULT_TAG = "SPC";

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }

    public static void v(String msg) {
        v(DEFAULT_TAG, msg);
    }

    public static boolean isDebugMode(){
        File file = new File(FileUtils.getInnerSDCardPath() + File.separator + "test.debug");
        if(file.exists()){
            return true;
        }
        return false;
    }

    public static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (DEBUG)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (DEBUG)
            Log.v(tag, msg);
    }
}
