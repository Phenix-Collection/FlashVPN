package com.polestar.ad;

/**
 * Created by guojia on 2016/12/11.
 */

import android.util.Log;


public class AdLog {

    private static final String DEFAULT_TAG = "pole_ad";

    public static void d(String tag, Object o) {
        if (AdConstants.DEBUG) {
            Log.d(tag, String.valueOf(o));
        }
    }

    public static void d(Object o) {
        if (AdConstants.DEBUG) {
            Log.d(DEFAULT_TAG, String.valueOf(o));
        }
    }

    public static void e(String tag, Object o) {
        if (AdConstants.DEBUG) {
            Log.e(tag, String.valueOf(o));
        }
    }

    public static void e(Object o) {
        if (AdConstants.DEBUG) {
            e(DEFAULT_TAG, o);
        }
    }

    public static void i(String tag, Object o) {
        if (AdConstants.DEBUG) {
            Log.i(tag, String.valueOf(o));
        }
    }

    public static void i(Object o) {
        if (AdConstants.DEBUG) {
            i(DEFAULT_TAG, o);
        }
    }



}
