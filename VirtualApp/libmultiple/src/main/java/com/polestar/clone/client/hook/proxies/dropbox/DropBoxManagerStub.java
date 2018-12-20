package com.polestar.clone.client.hook.proxies.dropbox;

import android.content.Context;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import mirror.com.android.internal.os.IDropBoxManagerService;

/**
 * @author Lody
 */
public class DropBoxManagerStub extends BinderInvocationProxy {
	public DropBoxManagerStub() {
		super(IDropBoxManagerService.Stub.asInterface, Context.DROPBOX_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ResultStaticMethodProxy("getNextEntry", null));
	}
}
