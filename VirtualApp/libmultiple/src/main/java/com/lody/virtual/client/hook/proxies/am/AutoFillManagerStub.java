package com.lody.virtual.client.hook.proxies.am;


import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;

public class AutoFillManagerStub extends BinderInvocationProxy {
    public AutoFillManagerStub( ) {
        super(IAutoFillManager.Stub.asInterface, "autofill");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("isServiceSupported",false));
        addMethodProxy(new ResultStaticMethodProxy("isServiceEnabled",false));
    }
}
