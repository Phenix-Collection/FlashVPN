
package com.polestar.imageloader.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.polestar.imageloader.ImageLoader;
import com.polestar.imageloader.ProgressRecorder;


public abstract class BaseLazyLoadImageView extends ImageView {

	protected String currentUrl;
	protected String targetUrl;

	private ImageLoadCompleteCallback mLoadCompleteCallback;
	
	public BaseLazyLoadImageView(Context context) {
		super(context);
	}

	public BaseLazyLoadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void requestDisplayURL(String url) {
		if (TextUtils.isEmpty(url)) {
			throw new IllegalArgumentException("the url can not be NULL");
		}

		if (url.equals(currentUrl)) {
			return;
		}
		targetUrl = url;
		ImageLoader.getInstance().displayImage(this, url);
	}

	public void setImageBitmap(Bitmap bm, String url) {
		if (mLoadCompleteCallback != null){
			if (!mLoadCompleteCallback.loadComplete(this, new BitmapDrawable(getResources(), bm))){
				super.setImageBitmap(bm);
			}
		}else{
			super.setImageBitmap(bm);
		}
		
		currentUrl = url;
		ProgressRecorder.getInstance().setProgress(url, 1.0f);
	}

	public void setImageDrawable(Drawable drawable, String url) {
		super.setImageDrawable(drawable);
		currentUrl = url;
	}

	public void setImageResource(int resId, String url) {
		super.setImageResource(resId);
		currentUrl = url;
	}

	public synchronized String getCurrentBitmapUrl() {
		return currentUrl;
	}

	public void setImageLoadCompleteCallback(ImageLoadCompleteCallback l){
		this.mLoadCompleteCallback = l;
	}
	
	public abstract void useDefaultBitmap();

	public abstract boolean setImageBitmapIfNeeds(Bitmap bm, String url);

	public abstract boolean isUrlNeeded(String url);

	@Override
	protected void onDraw(Canvas canvas) {
		// to prevent the bitmap recycle crash!
		final Drawable drawable = getDrawable();
		if (drawable != null && drawable instanceof BitmapDrawable) {
			final Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if (bitmap != null) {
				if (bitmap.isRecycled()) {
					requestDisplayURL(targetUrl);
					return;
				}
			} else {
				requestDisplayURL(targetUrl);
				return;
			}
		}
		super.onDraw(canvas);
	}

	public static interface ImageLoadCompleteCallback{
		public boolean loadComplete(BaseLazyLoadImageView view, Drawable d);
	}
}
