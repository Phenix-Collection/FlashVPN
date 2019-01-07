package com.mobile.earnings.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class ConnectionChecker{

	public static boolean isConnectionEnable(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null;
	}

}
