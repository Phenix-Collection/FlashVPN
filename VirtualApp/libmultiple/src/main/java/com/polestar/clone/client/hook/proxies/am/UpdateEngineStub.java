package com.polestar.clone.client.hook.proxies.am;

import android.os.*;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;

import mirror.android.os.ServiceManager;

public class UpdateEngineStub extends BinderInvocationProxy {

    public static class FakeUpdateEngine extends android.os.IUpdateEngine.Stub{
        @Override
        public void applyPayload(String url, long payload_offset, long payload_size, String[] headerKeyValuePairs) throws RemoteException {

        }

        @Override
        public boolean bind(IUpdateEngineCallback callback) throws RemoteException {
            return false;
        }

        @Override
        public boolean unbind(IUpdateEngineCallback callback) throws RemoteException {
            return false;
        }

        @Override
        public void suspend() throws RemoteException {

        }

        @Override
        public void resume() throws RemoteException {

        }

        @Override
        public void cancel() throws RemoteException {

        }

        @Override
        public void resetStatus() throws RemoteException {

        }

        @Override
        public boolean verifyPayloadApplicable(String metadataFilename) throws RemoteException {
            return false;
        }
    }

    public UpdateEngineStub() {
        super(new FakeUpdateEngine(), "android.os.UpdateEngineService");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
    }

    @Override
    public void inject() throws Throwable {
        if(ServiceManager.checkService.call(new Object[]{"android.os.UpdateEngineService"}) == null) {
            try {
                super.inject();
            }catch (Throwable ex) {

            }
        }
    }
}
