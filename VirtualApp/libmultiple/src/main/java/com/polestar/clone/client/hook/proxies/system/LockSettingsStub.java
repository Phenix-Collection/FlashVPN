package com.polestar.clone.client.hook.proxies.system;
import com.android.internal.widget.ILockSettings;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;

import mirror.android.os.ServiceManager;

public class LockSettingsStub extends BinderInvocationProxy {
    static class EmptyLockSettings extends ILockSettings.Stub {
        EmptyLockSettings() {
            super();
        }

        public int[] getRecoverySecretTypes() {
            return new int[0];
        }

        public void setRecoverySecretTypes(int[] arg1) {
        }
    }

    private static final String SERVICE_NAME = "lock_settings";

    public LockSettingsStub() {
        super(new EmptyLockSettings(), SERVICE_NAME);
    }

    @Override
    public void inject() {
        if(ServiceManager.checkService.call(new Object[]{"lock_settings"}) == null) {
            try {
                super.inject();

            }catch (Throwable ex) {

            }
        }
    }
}
