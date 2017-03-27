package com.lody.virtual.client.hook.providers;

import android.net.Uri;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class DownloadProviderHook extends ExternalProviderHook {

	public DownloadProviderHook(Object base) {
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
			return null;
		}
	}
}
