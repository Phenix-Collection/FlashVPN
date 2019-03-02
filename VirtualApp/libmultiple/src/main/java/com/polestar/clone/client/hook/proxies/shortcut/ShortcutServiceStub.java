package com.polestar.clone.client.hook.proxies.shortcut;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.content.pm.IShortcutService;

/**
 * @author Lody
 */
public class ShortcutServiceStub extends BinderInvocationProxy {


    private final static String TAG = "shortcut";
    public ShortcutServiceStub() {
        super(IShortcutService.Stub.asInterface, "shortcut");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("createShortcutResultIntent") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return null;
                }
            }
        });

        addMethodProxy(new ReplaceCallingPkgMethodProxy("updateShortcuts" ) {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return true;
                }
            }
        });

        addMethodProxy(new ReplaceCallingPkgMethodProxy("requestPinShortcut" ) {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return true;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getManifestShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getPinnedShortcuts") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return null;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getDynamicShortcuts") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return null;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("setDynamicShortcuts"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return true;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addDynamicShortcuts"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
                    return true;
                }
            }
        });
        addMethodProxy(new ReplaceCallingPkgMethodProxy("removeDynamicShortcuts"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex){
                    VLog.e(TAG, ex);
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
                    VLog.e(TAG, ex);
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

        addMethodProxy(new ReplaceCallingPkgMethodProxy("hasShortcutHostPermission"));
    }
}
