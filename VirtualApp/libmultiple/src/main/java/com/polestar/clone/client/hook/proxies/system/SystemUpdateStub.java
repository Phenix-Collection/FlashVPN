package com.polestar.clone.client.hook.proxies.system;

/**
 * Created by guojia on 2019/1/12.
 */

import android.os.Bundle;
import android.os.ISystemUpdateManager;
import android.os.PersistableBundle;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;

import mirror.android.os.ServiceManager;

public class SystemUpdateStub extends BinderInvocationProxy {
    static class EmptySystemUpdateManagerImpl extends ISystemUpdateManager.Stub {
        EmptySystemUpdateManagerImpl() {
            super();
        }

        public Bundle retrieveSystemUpdateInfo() {
            Bundle v0 = new Bundle();
            v0.putInt("status", 0);
            return v0;
        }

        public void updateSystemUpdateInfo(PersistableBundle arg1) {
        }
    }

    private static final String SERVICE_NAME = "system_update";

    public SystemUpdateStub() {
        super(new EmptySystemUpdateManagerImpl(), SERVICE_NAME);
    }

    @Override
    public void inject(){
        if(ServiceManager.checkService.call(SERVICE_NAME) == null) {
            try {
                super.inject();

            }catch (Throwable ex) {

            }
        }
    }
}
