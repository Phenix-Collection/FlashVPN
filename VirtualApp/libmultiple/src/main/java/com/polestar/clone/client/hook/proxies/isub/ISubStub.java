package com.polestar.clone.client.hook.proxies.isub;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;

import java.lang.reflect.Method;

import mirror.com.android.internal.telephony.ISub;

/**
 * @author Lody
 */
public class ISubStub extends BinderInvocationProxy {

    public ISubStub() {
        super(ISub.Stub.asInterface, "isub");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getAllSubInfoList"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return null;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getAllSubInfoCount") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return 0;
                }
            }
        });
        addMethodProxy(new ISubReplaceLastPkgMethodProxy("getActiveSubscriptionInfo"));
        addMethodProxy(new ISubReplaceLastPkgMethodProxy("getActiveSubscriptionInfoForIccId"));
        addMethodProxy(new ISubReplaceLastPkgMethodProxy("getActiveSubscriptionInfoForSimSlotIndex"));
        addMethodProxy(new ISubReplaceLastPkgMethodProxy("getActiveSubscriptionInfoList"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubInfoCount"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex) {
                    return  0;
                }
            }
        });
        addMethodProxy(new ISubReplaceLastPkgMethodProxy("getSubscriptionProperty"));
    }

    private class ISubReplaceLastPkgMethodProxy extends  ReplaceLastPkgMethodProxy {
        public ISubReplaceLastPkgMethodProxy(String name) {
            super(name);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            try {
                return super.call(who, method, args);
            }catch (Throwable ex) {
                return  null;
            }
        }
    }
}
