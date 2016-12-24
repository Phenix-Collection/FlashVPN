package com.lody.virtual.client.hook.patchs.connectivity;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.ConnectivityBinderDelegate;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 * @author legency
 */
public class ConnectivityPatch extends PatchDelegate<ConnectivityBinderDelegate> {

    @Override
    protected ConnectivityBinderDelegate createHookDelegate() {
        return new ConnectivityBinderDelegate();
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean isEnvBad() {
        return ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE) != getHookDelegate();
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        VLog.d("ConnectivityPatch", "onBindHooks");
        if (Build.VERSION.SDK_INT >= KITKAT) {
            addHook(new CheckMobileProvisioning());
        }
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
}
