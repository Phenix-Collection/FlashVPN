package com.polestar.clone.client.hook.proxies.appops;

import com.polestar.clone.GmsSupport;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.client.hook.proxies.location.MethodProxies;

import java.lang.reflect.Method;

import mirror.com.android.internal.app.ISmtOpsService;

/**
 * Created by guojia on 2019/3/30.
 */

@Inject(value=MethodProxies.class) public class SmtOpsManagerStub extends BinderInvocationProxy {
    public SmtOpsManagerStub() {
        super(ISmtOpsService.Stub.asInterface, "smtops");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new BaseMethodProxy("checkOperation", 1, 2));
        addMethodProxy(new BaseMethodProxy("noteOperation", 1, 2));
        addMethodProxy(new BaseMethodProxy("startOperation", 2, 3));
        addMethodProxy(new BaseMethodProxy("finishOperation", 2, 3));
        addMethodProxy(new BaseMethodProxy("startWatchingMode", -1, 1));
        addMethodProxy(new BaseMethodProxy("checkPackage", 0, 1){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if(GmsSupport.isGmsFamilyPackage((String)args[1])) {
                    return 0;
                }
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new BaseMethodProxy("getOpsForPackage", 0, 1));
        addMethodProxy(new BaseMethodProxy("setMode", 1, 2));
        addMethodProxy(new BaseMethodProxy("checkAudioOperation", 2, 3));
        addMethodProxy(new BaseMethodProxy("setAudioRestriction", 2, -1));
        addMethodProxy(new ReplaceLastPkgMethodProxy("resetAllModes"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if (args[0] instanceof Integer) {
                    //userId
                    args[0] = 0;
                }
                return 0;
            }
        });
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "noteProxyOperation";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                // TODO check whether all the operation can return allowed
                return 0;
            }
        });
    }

    private class BaseMethodProxy extends StaticMethodProxy {
        final int pkgIndex;
        final int uidIndex;

        BaseMethodProxy(String name, int uidIndex, int pkgIndex) {
            super(name);
            this.pkgIndex = pkgIndex;
            this.uidIndex = uidIndex;
        }

        @Override
        public boolean beforeCall(Object who, Method method, Object... args) {
            if (pkgIndex != -1 && args.length > pkgIndex && args[pkgIndex] instanceof String) {
                args[pkgIndex] = getHostPkg();
            }
            if (uidIndex != -1 && args[uidIndex] instanceof Integer) {
                args[uidIndex] = getRealUid();
            }
            return true;
        }
    }
}
