package com.polestar.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import com.polestar.ad.AdLog;
import com.polestar.imageloader.widget.BaseLazyLoadImageView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class ImageLoader implements Callback {

	private static ImageLoader INSTANCE;

	public synchronized static ImageLoader getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ImageLoader();
		}
		return INSTANCE;
	}

	private MemoryCache mCache;
	TaskManager mTaskManager;
	Handler mHandler;

	public MemoryCache getMemoryCache() {
		return mCache;
	}

	private HashMap<String, HashSet<WeakReference<BaseLazyLoadImageView>>> mImageUrlMap;
	private HashSet<String> mWifiOnlyUrls = new HashSet<String>();
	private boolean mIsWifiOnlyOn = false;

	public static final int BITMAP_LOADED = 1000;

	private ImageLoader() {
		mCache = MemoryCache.getInstance();
		mImageUrlMap = new HashMap<String, HashSet<WeakReference<BaseLazyLoadImageView>>>();
		mHandler = new Handler(this);
		mTaskManager = new TaskManager(mHandler);
		loadWifiOnlySettings();
	}

	private void loadWifiOnlySettings() {

	}

	public void setWifiOnlyEnable(boolean isEnable) {
		mIsWifiOnlyOn = isEnable;
	}

	public void cacheImageViewWithUrl(BaseLazyLoadImageView img, String url) {
		synchronized (mImageUrlMap) {
			if (mImageUrlMap.containsKey(url)) {
				HashSet<WeakReference<BaseLazyLoadImageView>> set = mImageUrlMap
						.get(url);
				boolean existImageView = false;
				for (Iterator<WeakReference<BaseLazyLoadImageView>> i = set
						.iterator(); i.hasNext();) {
					WeakReference<BaseLazyLoadImageView> imageViewReference = i
							.next();
					BaseLazyLoadImageView imageView = imageViewReference.get();
					if (imageView == null) {
						i.remove();
						continue;
					}

					if (img == imageView) {
						existImageView = true;
						break;
					}
				}

				if (!existImageView) {
					set.add(new WeakReference<BaseLazyLoadImageView>(img));
				}
			} else {
				HashSet<WeakReference<BaseLazyLoadImageView>> set = new HashSet<WeakReference<BaseLazyLoadImageView>>();
				set.add(new WeakReference<BaseLazyLoadImageView>(img));
				mImageUrlMap.put(url, set);
			}
		}
	}

	public void displayImage(BaseLazyLoadImageView img, String url) {
		if (displayImageFromCache(img, url)) {
			return;
		}

		cacheImageViewWithUrl(img, url);
		img.useDefaultBitmap();
		// use task to load the bitmap
		mTaskManager.startLoad(img.getContext().getApplicationContext(), url);
	}

	public void doPreLoad(Context context, String url){
		if (url != null && !DiskCacheUtils.isDiskCacheExist(context, url)) {
			mTaskManager.startLoad(context.getApplicationContext(), url);
		}
	}

	private boolean displayImageFromCache(BaseLazyLoadImageView img, String url) {
		final Bitmap bitmap = mCache.getBitmap(url);
		if (bitmap != null) {
			img.setImageBitmap(bitmap, url);
			return true;
		}

		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BITMAP_LOADED:
			processBitmapLoaded(msg);
			return true;
		}
		return false;
	}

	private void processBitmapLoaded(Message msg) {
		Bitmap bitmap = (Bitmap) msg.obj;
		if (bitmap == null || bitmap.isRecycled()) {
			return;
		}
		String url = msg.getData().getString("url");
		boolean isUseFullBitmap = false;
		synchronized (mImageUrlMap) {
			if (mImageUrlMap.containsKey(url)) {
				HashSet<WeakReference<BaseLazyLoadImageView>> set = mImageUrlMap
						.get(url);
				for (Iterator<WeakReference<BaseLazyLoadImageView>> i = set
						.iterator(); i.hasNext();) {
					WeakReference<BaseLazyLoadImageView> imageViewReference = i
							.next();
					BaseLazyLoadImageView imageView = imageViewReference.get();
					if (imageView != null) {
						if (imageView.setImageBitmapIfNeeds(bitmap, url)) {
							isUseFullBitmap = true;
						}
					}
					i.remove();
				}
			}
		}

		if (isUseFullBitmap) {
			mCache.putBitmap(url, bitmap);
		} else {
			bitmap.recycle();
		}
	}

	public void cleanup() {
		mWifiOnlyUrls.clear();
		mCache.recycle();
		mTaskManager.cancelAllTask();
		mImageUrlMap.clear();
	}

	public void onLowMemory() {
		AdLog.e("ImageLoader onLowMemory");
	}

	public HashSet<String> getWifiOnlySet() {
		return mWifiOnlyUrls;
	}

	public boolean isWifiOnlyOn() {
		return mIsWifiOnlyOn;
	}

}
