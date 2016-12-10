package com.polestar.imageloader.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.polestar.imageloader.DiskCacheUtils;
import com.polestar.imageloader.ImageLoader;
import com.polestar.imageloader.MemoryCache;


public class BasicLazyLoadImageView extends BaseLazyLoadImageView {

	int mDefaultResource = 0;
	private IProcessBitmap mIProcessBitmap;

	public BasicLazyLoadImageView(Context context) {
		super(context);
	}

	public BasicLazyLoadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void useDefaultBitmap() {
		if (mDefaultResource == 0) {
			setImageDrawable(new ColorDrawable(0), null);
		} else {
			setImageResource(mDefaultResource);
		}
	}

	public void setIProcessBitmap(IProcessBitmap i){
		this.mIProcessBitmap = i;
	}
	
	/*
	 * 设置默认图片resource
	 */
	public void setDefaultResource(int defaultResource) {
		mDefaultResource = defaultResource;
	}

	/*
	 * 在主UI线程中请求图片资源。仅仅在于点击这种耗时操作不敏感的情况下 目前用在了时间线的动画场景
	 */
	public void requestDisplayURLOnUIThread(String url) {
		if (TextUtils.isEmpty(url)) {
			useDefaultBitmap();
			return;
		}

		Bitmap bitmap = MemoryCache.getInstance().getBitmap(url);
		if (bitmap != null) {
			targetUrl = url;
			setImageBitmap(bitmap);
			return;
		}

		bitmap = DiskCacheUtils.getBitmap(getContext().getApplicationContext(), url);
		if (bitmap != null) {
			MemoryCache.getInstance().put(url, bitmap);
			targetUrl = url;
			setImageBitmap(bitmap);
		} else {
			useDefaultBitmap();
		}
	}

	private final void requestDisplayURL(String url, boolean wifiOnly) {
		if (wifiOnly) {
			ImageLoader.getInstance().getWifiOnlySet().add(url);
		} else {
			ImageLoader.getInstance().getWifiOnlySet().remove(url);
		}
		if (TextUtils.isEmpty(url)) {
			useDefaultBitmap();
			return;
		}
		super.requestDisplayURL(url);
	}

	public final void requestDisplayURLWifiOnly(String url) {
		requestDisplayURL(url, true);
	}

	@Override
	public final void requestDisplayURL(String url) {
		requestDisplayURL(url, false);
	}

	@Override
	public final boolean setImageBitmapIfNeeds(Bitmap bm, String url) {
		if (url.equals(targetUrl)) {
			if (mIProcessBitmap != null) {
				bm = mIProcessBitmap.processBitmap(bm);
			}
			this.setImageBitmap(bm, url);
			return true;
		}
		return false;
	}

	@Override
	public final boolean isUrlNeeded(String url) {
		return url.equals(targetUrl);
	}

	// 在图片被网络上得到之后。需要被处理
	public static interface IProcessBitmap {
		Bitmap processBitmap(Bitmap bitmap);
	}

}
