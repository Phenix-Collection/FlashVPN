package com.polestar.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.util.UUID;

public class DiskCacheUtils {

	public static File getCachePath(Context context, String url) {
		if(url.startsWith("/")){
			return new File(url);
		}
		
		return FileUtils.getImageCache(context,url);
	}

	public static File newRandomCacheFile(Context context) {
		return getCachePath(context, UUID.randomUUID().toString());
	}

	public static boolean isDiskCacheAviable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	public static boolean isDiskCacheExist(Context context, String url) {
		File cacheFileDir = getCachePath(context,url);
		return cacheFileDir.exists();
	}

	public static Bitmap getBitmap(Context context, String url) {
		File file = getCachePath(context, url);
		if (!file.exists()) {
			return null;
		}
		return DiskBitmapLoadHelper.decodeSampledBitmapFromFile(file
				.getAbsolutePath());
	}

	public static void cleanup(Context context) {
		File cacheDir = FileUtils.getExFileDir(context, FileUtils.EX_IMAGE);
		File files[] = cacheDir.listFiles();
		for (File f : files) {
			f.delete();
		}
	}

}
