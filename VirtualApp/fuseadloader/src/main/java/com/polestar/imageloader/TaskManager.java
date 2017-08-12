package com.polestar.imageloader;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.polestar.imageloader.task.ImageDiskLoadTask;
import com.polestar.imageloader.task.ImageNetworkLoadTask;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;


public class TaskManager {

	static final String TAG = "TaskManager";

	private final ThreadPoolExecutor mNetworkExecutor;
	private final ThreadPoolExecutor mDiskPoolExecutor;
	private final ConcurrentHashMap<String, WeakReference<ImageLoadTask>> mDownloadingUrls;

	Handler mHandler;

	public TaskManager(Handler handler) {
		mHandler = handler;
		mNetworkExecutor = ThreadPoolFactory.newNetworkPoolExecutor();
		mDiskPoolExecutor = ThreadPoolFactory.newDiskPoolExecutor();
		mDownloadingUrls = new ConcurrentHashMap<String, WeakReference<ImageLoadTask>>();
	}

	private boolean isUrlLoading(String url) {
		if (!mDownloadingUrls.containsKey(url)) {
			return false;
		}

		final WeakReference<ImageLoadTask> taskReference = mDownloadingUrls
				.get(url);
		final ImageLoadTask task = taskReference.get();
		if (task == null || task.isFinished()) {
			return false;
		}

		return true;
	}

	public void startLoad(Context context, String url) {
		if (isUrlLoading(url)) {
			return;
		}

		ImageLoadTask task = null;
		final Uri uri = Uri.parse(url);
		final String schema = uri.getScheme();
		if (schema == null) {
			task = new ImageDiskLoadTask(mHandler, url, context);
			mDiskPoolExecutor.execute(task);
		} else if (schema.equals("http") || schema.equals("https")) {
			if (DiskCacheUtils.isDiskCacheExist(context, url)) {
				task = new ImageDiskLoadTask(mHandler, url, context);
				mDiskPoolExecutor.execute(task);
			} else {
				ImageLoader imageLoader = ImageLoader.getInstance();
				if (!(imageLoader.isWifiOnlyOn()
						&& imageLoader.getWifiOnlySet().contains(url) && !isWifi())) {
					task = new ImageNetworkLoadTask(mHandler, url, context);
					mNetworkExecutor.execute(task);
				}

			}
		} else {
			task = UriTaskDispather.onSchema(schema, mHandler, url);
			if (task != null) {
				mNetworkExecutor.execute(task);
			}
		}

		if (task != null) {
			mDownloadingUrls.put(url, new WeakReference<ImageLoadTask>(task));
		}
	}

	private static boolean isWifi() {
		// ConnectivityManager connectivityManager = (ConnectivityManager)
		// AppContext
		// .getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		// NetworkInfo activeNetInfo =
		// connectivityManager.getActiveNetworkInfo();
		// if (activeNetInfo != null
		// && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
		// return true;
		// }
		// return false;
		return false;
	}

	public void cancelAllTask() {
		Collection<WeakReference<ImageLoadTask>> tasksReference = mDownloadingUrls
				.values();
		for (WeakReference<ImageLoadTask> taskReference : tasksReference) {
			ImageLoadTask task = taskReference.get();
			if (task != null) {
				task.cancel();
			}
		}
	}

}
