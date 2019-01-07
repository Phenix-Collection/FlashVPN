package com.polestar.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkFetchUtils {

	public static final String TAG = "NetworkFetchUtils";

	static final int CONNECTION_TIMEOUT = 20000;
	static final int SO_TIMEOUT = 20000;

	public static Bitmap fetch(Context context, String url) {
		Bitmap bitmap = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			bitmap = legacyFetch(context, url);
		} else {
			bitmap = urlFetch(context, url);
		}
		return bitmap;
	}

	private static Bitmap legacyFetch(Context context, String url) {
		final HttpClient client = (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) ? new DefaultHttpClient()
				: AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);
		client.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				SO_TIMEOUT);
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				// Log.w(TAG, "Error " + statusCode
				// + " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			long total = entity.getContentLength();
			float progress = 0.0f;
			File tmpFile = null;
			if (entity != null) {
				InputStream inputStream = null;
				FileOutputStream fos = null;
				try {
					inputStream = entity.getContent();
					if (DiskCacheUtils.isDiskCacheAviable()) {
						tmpFile = DiskCacheUtils.newRandomCacheFile(context);
						if (tmpFile != null) {
							fos = new FileOutputStream(tmpFile);
							final BufferedInputStream bis = new BufferedInputStream(
									inputStream, BUFF_SIZE);
							final BufferedOutputStream bos = new BufferedOutputStream(
									fos, BUFF_SIZE);
							byte[] buff = new byte[BUFF_SIZE];
							int len;
							int downloaded = 0;
							while ((len = bis.read(buff)) > 0) {
								bos.write(buff, 0, len);
								downloaded += len;
								progress = 1.0f * downloaded / total;
								ProgressRecorder.getInstance().setProgress(url,
										progress);
							}
							bos.flush();
							fos.close();
							inputStream.close();
							Bitmap bitmap = DiskBitmapLoadHelper
									.decodeSampledBitmapFromFile(tmpFile
											.getAbsolutePath());
							if (bitmap != null) {
								File cacheFile = DiskCacheUtils
										.getCachePath(context, url);
								tmpFile.renameTo(cacheFile);
							}
							return bitmap;
						}
					} else {
						return BitmapFactory
								.decodeStream(new FlushedInputStream(
										inputStream));
					}

				} finally {
					if (tmpFile != null) {
						tmpFile.delete();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			// Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			// Log.w(TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			// Log.w(TAG, "Error while retrieving bitmap from " + url, e);
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}
		return null;
	}

	private static Bitmap urlFetch(Context context, String url) {
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setReadTimeout(SO_TIMEOUT);
			int rspCode = conn.getResponseCode();
			if (rspCode != 200) {
				// Log.w(TAG, "Error " + rspCode
				// + " while retrieving bitmap from " + url);
				return null;
			}
			File tmpFile = null;
			InputStream inputStream = null;
			FileOutputStream fos = null;
			inputStream = conn.getInputStream();
			long total = conn.getContentLength();
			float progress = 0;
			try {
				if (DiskCacheUtils.isDiskCacheAviable()) {
					tmpFile = DiskCacheUtils.newRandomCacheFile(context);
					// Log.d(TAG, "path:" + tmpFile.getAbsolutePath() + " "
					// + tmpFile.canWrite());
					if (tmpFile != null) {
						fos = new FileOutputStream(tmpFile);
						final BufferedInputStream bis = new BufferedInputStream(
								inputStream, BUFF_SIZE);
						final BufferedOutputStream bos = new BufferedOutputStream(
								fos, BUFF_SIZE);
						byte[] buff = new byte[BUFF_SIZE];
						int len;
						int downloaded = 0;
						while ((len = bis.read(buff)) > 0) {
							bos.write(buff, 0, len);
							downloaded += len;
							progress = 1.0f * downloaded / total;
							ProgressRecorder.getInstance().setProgress(url,
									progress);
						}
						bos.flush();
						fos.close();
						inputStream.close();
						inputStream = null;
						fos = null;
						Bitmap bitmap = DiskBitmapLoadHelper
								.decodeSampledBitmapFromFile(tmpFile
										.getAbsolutePath());
						if (bitmap != null) {
							File cacheFile = DiskCacheUtils.getCachePath(context,url);
							tmpFile.renameTo(cacheFile);
						}
						return bitmap;
					}
				} else {
					// Log.w(TAG, "the disk is not aviable!!!");
					return BitmapFactory.decodeStream(new FlushedInputStream(
							inputStream));
				}

			} finally {
				if (inputStream != null) {
					inputStream.close();
				}

				if (fos != null) {
					fos.close();
				}

				if (tmpFile != null) {
					tmpFile.delete();
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// Log.w(TAG, "Error while retrieving bitmap from " + url, e);
		} catch (Exception e) {
			e.printStackTrace();
			// Log.w(TAG, "Error while retrieving bitmap from " + url, e);
		}
		return null;
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	private static final int BUFF_SIZE = 8 * 1024;

	@SuppressWarnings("unused")
	private static void copy(InputStream is, OutputStream os)
			throws IOException {
		final BufferedInputStream bis = new BufferedInputStream(is, BUFF_SIZE);
		final BufferedOutputStream bos = new BufferedOutputStream(os, BUFF_SIZE);
		byte[] buff = new byte[BUFF_SIZE];
		int len;
		while ((len = bis.read(buff)) > 0) {
			bos.write(buff, 0, len);
		}
		bos.flush();
	}
}
