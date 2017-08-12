
package com.polestar.imageloader;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class ImageLoadTask implements Runnable {

	static final String TAG = "ImageLoadTask";

	public String targetUrl;

	public static final int STATE_IDLE = 0;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_FINISH = 2;

	private int mState;
	private Handler mHandler;
	private boolean mIsCanncelled;

	public boolean isFinished() {
		return mState == STATE_FINISH;
	}
	
	public ImageLoadTask(){
		mState = STATE_IDLE;
	}
	
	public void setTargetUrl(String url){
		targetUrl = url;
	}
	
	public void setHandler(Handler handler){
		mHandler = handler;
	}

	public ImageLoadTask(Handler handler, String url) {
		mState = STATE_IDLE;
		targetUrl = url;
		mHandler = handler;
	}

	public void cancel() {
		mIsCanncelled = true;
	}

	private Bitmap processCircle(Bitmap bitmap) {
		if (bitmap == null) {
			return bitmap;
		}
		Uri uri = Uri.parse(targetUrl);
		if ("true".equals(uri.getQueryParameter("circle"))) {
			final Bitmap circleBitmap = ImageLoaderUtils.getCircleBitmap(bitmap);
			bitmap.recycle();
			return circleBitmap;
		}

		return bitmap;
	}

	public void run() {
		if (mIsCanncelled) {
			mState = STATE_FINISH;
			return;
		}

		mState = STATE_RUNNING;
		Bitmap bitmap = null;
		try {
			bitmap = load(targetUrl);
			bitmap = processCircle(bitmap);
		} catch (OutOfMemoryError e) {
			System.gc();
		}

		if (bitmap != null && !mIsCanncelled) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("url", targetUrl);
			msg.setData(bundle);
			msg.obj = bitmap;
			msg.what = ImageLoader.BITMAP_LOADED;
			mHandler.sendMessage(msg);
		} else if (bitmap != null && mIsCanncelled) {
			bitmap.recycle();
			bitmap = null;
		}
		mState = STATE_FINISH;
	}

	protected abstract Bitmap load(String url);
}
