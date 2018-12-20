package com.polestar.clone.client.hook.proxies.input;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.Inject;

import mirror.com.android.internal.view.inputmethod.InputMethodManager;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)

public class InputMethodManagerStub extends BinderInvocationProxy {

	public InputMethodManagerStub() {
		super(
				InputMethodManager.mService.get(
						VirtualCore.get().getContext().getSystemService(Context.INPUT_METHOD_SERVICE)),
				Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public void inject() throws Throwable {
		Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		InputMethodManager.mService.set(inputMethodManager, getInvocationStub().getProxyInterface());
		getInvocationStub().replaceService(Context.INPUT_METHOD_SERVICE);
	}


	@Override
	public boolean isEnvBad() {
		Object inputMethodManager = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return InputMethodManager
				.mService.get(inputMethodManager) != getInvocationStub().getBaseInterface();
	}

}