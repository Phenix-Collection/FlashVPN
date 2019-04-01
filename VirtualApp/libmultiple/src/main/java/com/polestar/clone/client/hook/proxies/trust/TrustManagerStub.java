package com.polestar.clone.client.hook.proxies.trust;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastUserIdMethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import mirror.android.app.trust.ITrustManager;


/**
 * Created by guojia on 2019/4/1.
 */
//
//26interface ITrustManager {
//27    void reportUnlockAttempt(boolean successful, int userId);
//28    void reportUnlockLockout(int timeoutMs, int userId);
//29    void reportEnabledTrustAgentsChanged(int userId);
//30    void registerTrustListener(in ITrustListener trustListener);
//31    void unregisterTrustListener(in ITrustListener trustListener);
//32    void reportKeyguardShowingChanged();
//33    void setDeviceLockedForUser(int userId, boolean locked);
//34    boolean isDeviceLocked(int userId);
//35    boolean isDeviceSecure(int userId);
//36    boolean isTrustUsuallyManaged(int userId);
//37}

public class TrustManagerStub extends BinderInvocationProxy {
    public TrustManagerStub() {
        super(ITrustManager.Stub.TYPE, "trust");
    }
    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("reportUnlockAttempt", null));
        addMethodProxy(new ResultStaticMethodProxy("reportUnlockLockout", null));
        addMethodProxy(new ResultStaticMethodProxy("reportEnabledTrustAgentsChanged", null));
        addMethodProxy(new ResultStaticMethodProxy("registerTrustListener", null));
        addMethodProxy(new ResultStaticMethodProxy("unregisterTrustListener", null));
        addMethodProxy(new ResultStaticMethodProxy("reportKeyguardShowingChanged", null));
        addMethodProxy(new ResultStaticMethodProxy("setDeviceLockedForUser", null));
        addMethodProxy(new ReplaceLastUserIdMethodProxy("isDeviceLocked"));
        addMethodProxy(new ReplaceLastUserIdMethodProxy("isDeviceSecure"));
        addMethodProxy(new ReplaceLastUserIdMethodProxy("isTrustUsuallyManaged"));
    }
}
