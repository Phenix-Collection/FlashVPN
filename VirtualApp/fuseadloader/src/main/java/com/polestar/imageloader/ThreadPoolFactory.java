package com.polestar.imageloader;

import android.annotation.SuppressLint;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint("NewApi")
public class ThreadPoolFactory {
	private static final int NETWORK_CORE_POOL_SIZE = 2;
	private static final int DISK_CORE_POOL_SIZE = 3;
	private static final int NETWORK_MAXIMUM_POOL_SIZE = 5;
	private static final int DISK_MAXIMUM_POOL_SIZE = 5;
	private static final int KEEP_ALIVE = 60;

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "ImageLoader #" + mCount.getAndIncrement());
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		}
	};

	private static final BlockingQueue<Runnable> sNetworkWorkQueue = new LinkedBlockingQueue<Runnable>(
			1024);
	private static final BlockingQueue<Runnable> sDiskWorkQueue = new LinkedBlockingQueue<Runnable>(
			1024);

	public static ThreadPoolExecutor newNetworkPoolExecutor() {
		ThreadPoolExecutor networkPoolExecutor = new ThreadPoolExecutor(
				NETWORK_CORE_POOL_SIZE, NETWORK_MAXIMUM_POOL_SIZE, KEEP_ALIVE,
				TimeUnit.SECONDS, sNetworkWorkQueue, sThreadFactory);
//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
//			networkPoolExecutor.allowCoreThreadTimeOut(true);
//		}

		return networkPoolExecutor;
	}

	//Disk Executor Disable
	public static ThreadPoolExecutor newDiskPoolExecutor() {
		ThreadPoolExecutor diskPoolExecutor = new ThreadPoolExecutor(
				DISK_CORE_POOL_SIZE, DISK_MAXIMUM_POOL_SIZE, KEEP_ALIVE,
				TimeUnit.SECONDS, sDiskWorkQueue, sThreadFactory);
//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
//			diskPoolExecutor.allowCoreThreadTimeOut(true);
//		}
		return diskPoolExecutor;
	}

}
