package com.polestar.clone.client.hook.proxies.network;

import android.annotation.TargetApi;
import android.os.Build;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceUidMethodProxy;

import mirror.android.os.INetworkManagementService;

@TargetApi(Build.VERSION_CODES.M)
public class
NetworkManagementStub extends BinderInvocationProxy {

	public NetworkManagementStub() {
		super(INetworkManagementService.Stub.asInterface, "network_management");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceUidMethodProxy("setUidCleartextNetworkPolicy", 0));
	}
}
