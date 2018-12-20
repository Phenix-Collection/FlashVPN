package com.polestar.clone.client.hook.proxies.shortcut;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;

import java.lang.reflect.Method;

import mirror.android.content.pm.ILauncherApps;

/**
 * @author Lody
 */
public class ShortcutServiceStub extends BinderInvocationProxy {


    public ShortcutServiceStub() {
        super(ILauncherApps.Stub.asInterface, "shortcut");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getManifestShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("setDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addDynamicShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeDynamicShortcuts"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    return 0;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAllDynamicShortcuts"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    return 0;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getPinnedShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("updateShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("createShortcutResultIntent"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("disableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enableShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRemainingCallCount"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getRateLimitResetTime"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getIconMaxDimensions"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getMaxShortcutCountPerActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("reportShortcutUsed"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("onApplicationActive"));
    }
}
