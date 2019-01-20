package com.polestar.clone.client.hook.proxies.am;


import android.content.ComponentName;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;
import com.polestar.clone.helper.utils.ArrayUtils;
import com.polestar.clone.helper.utils.VLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AutoFillManagerStub extends BinderInvocationProxy {
    class ReplacePkgAndComponentProxy extends ReplaceLastPkgMethodProxy {
        ReplacePkgAndComponentProxy(String arg1) {
            super(arg1);
        }

        public boolean beforeCall(Object arg2, Method arg3, Object[] arg4) {
            this.replaceLastAppComponent(arg4, getHostPkg());
            return super.beforeCall(arg2, arg3, arg4);
        }

        private void replaceLastAppComponent(Object[] arg4, String arg5) {
            int v1 = ArrayUtils.indexOfLast(arg4, ComponentName.class);
            if(v1 != -1) {
                arg4[v1] = new ComponentName(arg5, arg4[v1].getClass().getName());
            }
        }
    }

    public AutoFillManagerStub( ) {
        super(IAutoFillManager.Stub.asInterface, "autofill");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        try {
            Object v1 = getContext().getSystemService("autofill");
            if(v1 == null) {
                throw new NullPointerException("AutoFillManagerInstance is null.");
            }

            Object v0_1 = getInvocationStub().getProxyInterface();
            if(v0_1 == null) {
                throw new NullPointerException("AutoFillManagerProxy is null.");
            }

            Field v2 = v1.getClass().getDeclaredField("mService");

            v2.setAccessible(true);
            v2.set(v1, v0_1);
        }
        catch(Throwable v0) {
            VLog.e("AutoFillManagerStub", "AutoFillManagerStub inject error.", v0);
            return;
        }
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ResultStaticMethodProxy("isServiceSupported",false));
        addMethodProxy(new ResultStaticMethodProxy("isServiceEnabled",false));
        addMethodProxy(new ReplacePkgAndComponentProxy("startSession"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    return super.call(who, method, args);
                }catch (Throwable ex) {
                    return Integer.MIN_VALUE;
                }
            }
        });
        addMethodProxy(new ReplacePkgAndComponentProxy("updateOrRestartSession"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("isServiceEnabled"));

//        addMethodProxy(new ResultStaticMethodProxy("isServiceSupported",false));
//        addMethodProxy(new ResultStaticMethodProxy("isServiceEnabled",false));
//        addMethodProxy(new ResultStaticMethodProxy("startSession",Integer.MIN_VALUE));
    }
}
