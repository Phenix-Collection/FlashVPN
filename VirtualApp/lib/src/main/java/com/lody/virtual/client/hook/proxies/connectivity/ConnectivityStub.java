package com.lody.virtual.client.hook.proxies.connectivity;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.net.IConnectivityManager;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * @author legency
 */
public class ConnectivityStub extends BinderInvocationProxy {

    public ConnectivityStub() {
        super(IConnectivityManager.Stub.asInterface, Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveNetworkInfo"));
        addMethodProxy(new ReportInetCondition());
        if (Build.VERSION.SDK_INT >= KITKAT) {
            addMethodProxy(new CheckMobileProvisioning());
        }
    }

    private static class ReportInetCondition extends MethodProxy {
        @Override
        public String getMethodName() {
            return "reportInetCondition";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("ConnectivityPatch", "reportInetCondition hooked");
            return null;
        }
    }

    private static class CheckMobileProvisioning extends MethodProxy {
        @Override
        public String getMethodName() {
            return "checkMobileProvisioning";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("ConnectivityPatch", "CheckMobileProvisioning hooked");
            return -1;
        }
    }
}
