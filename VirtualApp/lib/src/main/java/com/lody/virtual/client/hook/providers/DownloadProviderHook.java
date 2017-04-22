package com.lody.virtual.client.hook.providers;

import android.net.Uri;

import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 */

class DownloadProviderHook extends ExternalProviderHook {

	DownloadProviderHook(Object base) {
		super(base);
	}

	@Override
    protected void processArgs(Method method, Object... args) {
    }
    @Override
	public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		try {
			return super.insert(method, args);
		}catch (Exception e) {
			VLog.logbug("DownloadProviderHook", VLog.getStackTraceString(e));
			return new Uri.Builder().appendPath("0").build();
		}
	}
}
