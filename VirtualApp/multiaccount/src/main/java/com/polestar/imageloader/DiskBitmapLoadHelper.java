package com.polestar.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DiskBitmapLoadHelper {

	static final int MAX_WIDTH = 400;
	static final int MAX_HEIGHT = 300;

	static final int MIN_WIDTH = 200;
	static final int MIN_HEIGHT = 150;

	static final int SYSTEM_MAX_WIDTH = 600;
	static final int SYSTEM_MAX_HEIGHT = 450;

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		// 对于超长图系统有限制，对限制进行处理
		if (width < MIN_WIDTH || height < MIN_HEIGHT) {
			if (width > SYSTEM_MAX_WIDTH || height > SYSTEM_MAX_HEIGHT) {
				reqWidth = SYSTEM_MAX_WIDTH;
				reqHeight = SYSTEM_MAX_HEIGHT;
			} else {
				return 1;
			}
		}

		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromFile(String filename,
													 int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	public static Bitmap decodeSampledBitmapFromFile(String filename) {
		return decodeSampledBitmapFromFile(filename, MAX_WIDTH, MAX_HEIGHT);
	}

}
