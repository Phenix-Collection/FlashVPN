package com.polestar.imageloader;

import android.os.Handler;

import java.util.HashMap;

public class UriTaskDispather {

	public static final HashMap<String, Class<? extends ImageLoadTask>> sLoadTaskMap = new HashMap<String, Class<? extends ImageLoadTask>>();

	public static ImageLoadTask onSchema(String schema, Handler handler,
										 String url) {
		Class<? extends ImageLoadTask> loadTaskClass = sLoadTaskMap.get(schema);
		if (loadTaskClass == null) {
			return null;
		}
		try {
			ImageLoadTask task = loadTaskClass.newInstance();
			task.setHandler(handler);
			task.setTargetUrl(url);
			return task;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void putSchema(String schema,
			Class<? extends ImageLoadTask> klass) {
		sLoadTaskMap.put(schema, klass);
	}
}
