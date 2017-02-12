package com.lody.virtual.client.ipc;

import android.os.IBinder;

import com.lody.virtual.service.IPackageManager;
import com.lody.virtual.service.interfaces.IIntentFilterObserver;

/**
 * @author Lody
 */

public class VIntentFilterManager {
	private static IIntentFilterObserver mRemote;

	public static IIntentFilterObserver getInterface() {
		if (mRemote == null) {
			synchronized (VIntentFilterManager.class) {
				if (mRemote == null) {
					Object remote = getRemoteInterface();
					mRemote = LocalProxyUtils.genProxy(IIntentFilterObserver.class, remote, new LocalProxyUtils.DeadServerHandler() {
						@Override
						public Object getNewRemoteInterface() {
							mRemote = null;
							return getRemoteInterface();
						}
					});
				}
			}
		}
		return mRemote;
	}


	private static Object getRemoteInterface() {
		final IBinder binder = ServiceManagerNative.getService(ServiceManagerNative.INTENT_FILTER);
		return IIntentFilterObserver.Stub.asInterface(binder);
	}
}
