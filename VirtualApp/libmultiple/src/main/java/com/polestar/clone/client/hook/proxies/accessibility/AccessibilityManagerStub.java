package com.polestar.clone.client.hook.proxies.accessibility;

import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.os.VUserHandle;

import java.lang.reflect.Method;

import mirror.android.view.accessibility.IAccessibilityManager;

public class AccessibilityManagerStub extends BinderInvocationProxy {
    class ReplaceLastUserIdProxy extends StaticMethodProxy {
        public ReplaceLastUserIdProxy(String arg1) {
            super(arg1);
        }

        public boolean beforeCall(Object arg3, Method arg4, Object[] arg5) {
            int v0 = arg5.length - 1;
            if(v0 >= 0 && ((arg5[v0] instanceof Integer))) {
                arg5[v0] = Integer.valueOf(VUserHandle.getHostUserId());
            }

            return super.beforeCall(arg3, arg4, arg5);
        }
    }

    public AccessibilityManagerStub() {
        super(IAccessibilityManager.Stub.TYPE, "accessibility");
    }

    protected void onBindMethods() {
        super.onBindMethods();
        this.addMethodProxy(new ReplaceLastUserIdProxy("addClient"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("sendAccessibilityEvent"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("getInstalledAccessibilityServiceList"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("getEnabledAccessibilityServiceList"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("getWindowToken"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("interrupt"));
        this.addMethodProxy(new ReplaceLastUserIdProxy("addAccessibilityInteractionConnection"));
    }
}

