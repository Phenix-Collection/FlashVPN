package com.lody.virtual.client.hook.proxies.admin;

import android.accounts.Account;
import android.content.Context;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.proxies.account.AccountManagerStub;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.app.admin.IDevicePolicyManager;

/**
 * Created by guojia on 2017/7/7.
 */

public class DevicePolicyManagerStub extends BinderInvocationProxy {

    public DevicePolicyManagerStub() {
        super(IDevicePolicyManager.Stub.asInterface, Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        addMethodProxy(new getStorageEncryptionStatus());
    }

    private static class getStorageEncryptionStatus extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getStorageEncryptionStatus";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            args[0] = VirtualCore.get().getHostPkg();
            return method.invoke(who, args);
        }
    }
}
