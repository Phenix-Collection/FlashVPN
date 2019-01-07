package com.mobile.earnings.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class PackageUtil{

	private PackageUtil(){
		throw new AssertionError();
	}

	public static boolean isPackageDownloaded(String packageName, Context context){
		PackageManager pm = context.getPackageManager();
		try{
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch(PackageManager.NameNotFoundException e){
			Log.e("TAGA", "Exception: "+e.getMessage());
			return false;
		}
	}

}
