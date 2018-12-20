package com.polestar.clone.client.hook.proxies.am;


import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

public class AutoFillManagerStub extends BinderInvocationProxy {
    public AutoFillManagerStub( ) {
        super(IAutoFillManager.Stub.asInterface, "autofill");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("isServiceSupported",false));
        addMethodProxy(new ResultStaticMethodProxy("isServiceEnabled",false));
        addMethodProxy(new ResultStaticMethodProxy("startSession",Integer.MIN_VALUE));
    }
}
