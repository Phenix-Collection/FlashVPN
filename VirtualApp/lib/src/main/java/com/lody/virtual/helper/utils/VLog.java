package com.lody.virtual.helper.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.lody.virtual.BuildConfig;

import java.io.File;

/**
 * @author Lody
 *
 */
public class VLog {

	public interface IKeyLogger {
		void keyLog(Context context, String tag, String log);
		void logBug(String tag, String log);
	}
	public static IKeyLogger sKeyLogger = null;
	public static boolean OPEN_LOG = BuildConfig.DEBUG;
	public static String VTAG = "PLIB_";

	public static void keyLog(Context context, String tag, String log){
		if(sKeyLogger != null) {
			sKeyLogger.keyLog(context, tag, log);
		}
	}

	public static void openLog() {
		OPEN_LOG = true;
	}

	public static void setKeyLogger(IKeyLogger logger) {
		sKeyLogger = logger;
	}

	public class VKeyLogTag {
		public static final String VERROR = "verror";
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

	public static void d(String tag, String msg) {
		if (OPEN_LOG) {
			Log.d(VTAG+tag, msg);
		}
	}

	public static void w(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.w(VTAG+tag, String.format(msg, format));
		}
	}

	public static void w(String tag, String msg) {
		if (OPEN_LOG) {
			Log.w(VTAG+tag, msg);
		}
	}

	public static void e(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.e(VTAG+tag, String.format(msg, format));
		}
	}

	public static void e(String tag, String msg) {
		if (OPEN_LOG) {
			Log.e(VTAG+tag, msg);
		}
	}

	public static void v(String tag, String msg, Object... format) {
		if (OPEN_LOG) {
			Log.v(VTAG+tag, String.format(msg, format));
		}
	}

	public static void v(String tag, String msg ) {
		if (OPEN_LOG) {
			Log.v(VTAG+tag, msg);
		}
	}

	public static String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	public static void printStackTrace(String tag) {
		Log.e(tag, getStackTraceString(new Exception()));
	}

	public static void e(String tag, Throwable e) {
		Log.e(VTAG+tag, getStackTraceString(e));
	}
	public static void logbug(String tag, String msg) {
		if (sKeyLogger != null) {
			sKeyLogger.logBug(VTAG + tag, msg);
		} else {
			Log.e(VTAG + tag, msg);
		}
	}
}
