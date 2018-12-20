package com.polestar.clone.client.hook.proxies.am;

import android.os.Binder;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import mirror.android.os.ServiceManager;

public class UpdateEngineStub extends BinderInvocationProxy {

    public UpdateEngineStub() {
        super(IUpdateEngine.Stub.asInterface, "android.os.UpdateEngineService");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("bind", false));
        addMethodProxy(new ResultStaticMethodProxy("applyPayload", null));
        addMethodProxy(new ResultStaticMethodProxy("cancel", null));
        addMethodProxy(new ResultStaticMethodProxy("unbind", false));
        addMethodProxy(new ResultStaticMethodProxy("suspend", null));
        addMethodProxy(new ResultStaticMethodProxy("resetStatus", null));
        addMethodProxy(new ResultStaticMethodProxy("resume", null));
    }

    @Override
    public void inject() throws Throwable {
        ServiceManager.sCache.get().put(mServiceName, new Binder());
    }
}
