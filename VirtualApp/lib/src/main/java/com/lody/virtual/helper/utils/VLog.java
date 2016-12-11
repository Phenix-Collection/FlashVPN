package com.lody.virtual.helper.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * @author Lody
 *
 */
public class VLog {

	public static boolean OPEN_LOG = isDebugMode();
	public static String VTAG = "VAPP_";

	static boolean existDebugFile = false;
	public static boolean isDebugMode(){
		if (existDebugFile) return  true;
		File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "vapp.debug");
		if(file.exists()){
			existDebugFile = true;
			return true;
		}
		return false;
	}
	public static void i(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.i(VTAG+tag, String.format(msg, format));
		}
	}

	public static void d(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.d(VTAG+tag, String.format(msg, format));
		}
	}

	public static void w(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.w(VTAG+tag, String.format(msg, format));
		}
	}

	public static void e(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.e(VTAG+tag, String.format(msg, format));
		}
	}

	public static void v(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.v(VTAG+tag, String.format(msg, format));
		}
	}

	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	public static void printStackTrace(String tag) {
		Log.e(VTAG+tag, getStackTraceString(new Exception()));
	}

	public static void e(String tag, Throwable e) {
		Log.e(VTAG+tag, getStackTraceString(e));
	}
}
