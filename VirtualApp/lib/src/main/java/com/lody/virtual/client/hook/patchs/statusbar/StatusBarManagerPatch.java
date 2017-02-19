package com.lody.virtual.client.hook.patchs.statusbar;

import android.content.Context;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.statusbar.IStatusBarService;

/**
 * Created by guojia on 2016/12/18.
 */

public class StatusBarManagerPatch extends PatchBinderDelegate{

    public StatusBarManagerPatch() {
        super(IStatusBarService.Stub.TYPE, Context.STATUS_BAR_SERVICE);
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new Disable());
    }

    private static class Disable extends Hook {
        @Override
        public String getName() {
            return "disable";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("StatusBarManagerPatch", "Disable hooked");
            return null;
        }
    }
}
