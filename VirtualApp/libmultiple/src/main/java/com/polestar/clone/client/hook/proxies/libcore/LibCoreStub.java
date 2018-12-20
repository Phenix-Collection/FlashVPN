package com.polestar.clone.client.hook.proxies.libcore;

import com.polestar.clone.client.hook.base.MethodInvocationStub;
import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.MethodInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceUidMethodProxy;

import mirror.libcore.io.ForwardingOs;
import mirror.libcore.io.Libcore;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class LibCoreStub extends MethodInvocationProxy<MethodInvocationStub<Object>> {

    public LibCoreStub() {
        super(new MethodInvocationStub<Object>(getOs()));
    }

    private static Object getOs() {
        Object os = Libcore.os.get();
        if (ForwardingOs.os != null) {
            Object posix = ForwardingOs.os.get(os);
            if (posix != null) {
                os = posix;
            }
        }
        return os;
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceUidMethodProxy("chown", 1));
        addMethodProxy(new ReplaceUidMethodProxy("fchown", 1));
        addMethodProxy(new ReplaceUidMethodProxy("getpwuid", 0));
        addMethodProxy(new ReplaceUidMethodProxy("lchown", 1));
        addMethodProxy(new ReplaceUidMethodProxy("setuid", 0));
    }

    @Override
    public void inject() throws Throwable {
        Libcore.os.set(getInvocationStub().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return getOs() != getInvocationStub().getProxyInterface();
    }
}
