package com.polestar.clone.client.hook.proxies.devicepolicy;

import android.content.Context;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;

import java.lang.reflect.Method;

import mirror.android.app.admin.IDevicePolicyManager;

/**
 * Created by wy on 2017/10/20.
 */

public class DevicePolicyManagerStub extends BinderInvocationProxy{
    public DevicePolicyManagerStub() {
        super(IDevicePolicyManager.Stub.asInterface, Context.DEVICE_POLICY_SERVICE);
    }

//    void setActiveAdmin(in ComponentName policyReceiver, boolean refreshing, int userHandle);
//    boolean isAdminActive(in ComponentName policyReceiver, int userHandle);
//    List<ComponentName> getActiveAdmins(int userHandle);
    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new GetStorageEncryptionStatus());
        addMethodProxy(new ResultStaticMethodProxy("getDeviceOwnerComponent",null));
        addMethodProxy(new ResultStaticMethodProxy("notifyPendingSystemUpdate",null));
        addMethodProxy(new ResultStaticMethodProxy("getActiveAdmins",null));
        addMethodProxy(new ResultStaticMethodProxy("isAdminActive",false));
        addMethodProxy(new ResultStaticMethodProxy("setActiveAdmin",null));
    }

    private static class GetStorageEncryptionStatus extends MethodProxy {

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
