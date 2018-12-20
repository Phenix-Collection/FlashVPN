package com.polestar.clone.client.hook.proxies.context_hub;

import android.os.Build;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import mirror.android.hardware.location.IContextHubService;

public class ContextHubServiceStub extends BinderInvocationProxy {

    public ContextHubServiceStub() {
        super(IContextHubService.Stub.asInterface, getServiceName());
    }

    private static String getServiceName() {
        return Build.VERSION.SDK_INT >= 26 ? "contexthub" : "contexthub_service";
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("registerCallback", 0));
        addMethodProxy(new ResultStaticMethodProxy("getContextHubHandles", new int[0]));
    }
}