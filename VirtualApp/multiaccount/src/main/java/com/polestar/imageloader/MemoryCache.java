package com.polestar.imageloader;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public class MemoryCache extends AvLruCache<String, Bitmap> {

	static final String TAG = "MemoryCache";
	static final int MAX_SIZE = 400;

	HashSet<WeakReference<Bitmap>> mCachedBitmaps;

	private static MemoryCache INSTANCE;

	public static MemoryCache getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MemoryCache();
		}
		return INSTANCE;
	}

	static final int CACHE_SIZE;
	static final int MIN_MEMORY_SIZE = 4 * 1024;
	static {
		CACHE_SIZE = MIN_MEMORY_SIZE;
	}

	public MemoryCache() {
		super(CACHE_SIZE);
		mCachedBitmaps = new HashSet<WeakReference<Bitmap>>();
	}

	@SuppressLint("NewApi")
	@Override
	protected int sizeOf(String key, Bitmap value) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return value.getWidth() * value.getHeight() * 4 / 1024;
		} else {
			return value.getByteCount() / 1024;
		}
	}

	public Bitmap getBitmap(String url) {
		Bitmap bitmap = get(url);
		return bitmap;
	}

	static boolean isBimapShouldCache(Bitmap bitmap) {
		// 缓存限制放宽
		// if (bitmap.getWidth() > MAX_SIZE || bitmap.getHeight() > MAX_SIZE) {
		// return false;
		// }
		return true;
	}

	public void putBitmap(String url, Bitmap bitmap) {
		if (isBimapShouldCache(bitmap)) {
			put(url, bitmap);
		}
		mCachedBitmaps.add(new WeakReference<Bitmap>(bitmap));
	}

	public void recycle() {
		evictAll();
		HashSet<WeakReference<Bitmap>> bitmaps = mCachedBitmaps;
		for (WeakReference<Bitmap> bitmapReference : bitmaps) {
			Bitmap bitmap = bitmapReference.get();
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}
		}
		bitmaps.clear();
	}
}
