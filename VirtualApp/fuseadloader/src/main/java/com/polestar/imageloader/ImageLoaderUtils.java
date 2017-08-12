package com.polestar.imageloader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

public class ImageLoaderUtils {
	static Paint sBitmapPaint;
	static {
		sBitmapPaint = new Paint();
		sBitmapPaint.setFilterBitmap(false);
	}

	public static Bitmap getCircleBitmap(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		final Bitmap dest = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		final Bitmap round = makeDst(bitmap.getWidth(), bitmap.getHeight());
		final Canvas canvas = new Canvas(dest);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setFilterBitmap(false);
		canvas.drawBitmap(round, 1, 1, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, 0, 0, paint);
		paint.setXfermode(null);
		round.recycle();
		return dest;
	}

	static Bitmap makeDst(int w, int h) {
		Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bm);
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		p.setColor(0xFFFFCC44);
		c.drawOval(new RectF(0, 0, w - 2, h - 2), p);
		return bm;
	}
}
