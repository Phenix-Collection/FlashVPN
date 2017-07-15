package com.polestar.imageloader;

import java.util.HashMap;

public class ProgressRecorder {

	private final HashMap<String, Float> mProgressURLMap;

	private static ProgressRecorder INSTANCE;

	private ProgressRecorder() {
		mProgressURLMap = new HashMap<String, Float>();
	}

	public static synchronized ProgressRecorder getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ProgressRecorder();
		}

		return INSTANCE;
	}

	public void setProgress(String url, float progress) {
		if (url != null) {			
			mProgressURLMap.put(url, progress);
		}
	}

	public float getProgress(String url) {
		final Float progress = mProgressURLMap.get(url);
		if (progress == null) {
			return 0;
		}

		return progress;
	}

}
