package com.lody.virtual.client.hook.patchs.connectivity;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.helper.utils.VLog;


import java.lang.reflect.Method;

import mirror.android.net.IConnectivityManager;

import static android.os.Build.VERSION_CODES.KITKAT;


/**
 * @author legency
 */
public class ConnectivityPatch extends PatchBinderDelegate {


    public ConnectivityPatch() {
        super(IConnectivityManager.Stub.TYPE, Context.CONNECTIVITY_SERVICE);
    }


    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        VLog.d("ConnectivityPatch", "onBindHooks");
        if (Build.VERSION.SDK_INT >= KITKAT) {
            addHook(new CheckMobileProvisioning());

        }
        addHook(new ReportInetCondition());
    }

    private static class CheckMobileProvisioning extends Hook {
        @Override
        public String getName() {
            return "checkMobileProvisioning";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("ConnectivityPatch", "CheckMobileProvisioning hooked");
            return -1;
        }
    }

    private static class ReportInetCondition extends Hook {
        @Override
        public String getName() {
            return "reportInetCondition";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("ConnectivityPatch", "reportInetCondition hooked");
            return null;
        }
    }
}
