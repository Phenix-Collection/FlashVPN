package com.polestar.clone.client.hook.proxies.system;
import android.app.PendingIntent;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.widget.ILockSettings;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;

import java.util.Map;

import mirror.android.os.ServiceManager;

public class LockSettingsStub extends BinderInvocationProxy {
    static class EmptyLockSettings extends ILockSettings.Stub {
        EmptyLockSettings() {
            super();
        }

        public int[] getRecoverySecretTypes() {
            return new int[0];
        }

        @Override
        public void setLong(String key, long value, int userId) throws RemoteException {

        }

        @Override
        public void setString(String key, String value, int userId) throws RemoteException {

        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
            return false;
        }

        @Override
        public long getLong(String key, long defaultValue, int userId) throws RemoteException {
            return 0;
        }

        @Override
        public String getString(String key, String defaultValue, int userId) throws RemoteException {
            return null;
        }

        @Override
        public void setLockCredential(String credential, int type, String savedCredential, int requestedQuality, int userId) throws RemoteException {

        }

        @Override
        public void resetKeyStore(int userId) throws RemoteException {

        }

        @Override
        public boolean checkVoldPassword(int userId) throws RemoteException {
            return false;
        }

        @Override
        public boolean havePattern(int userId) throws RemoteException {
            return false;
        }

        @Override
        public boolean havePassword(int userId) throws RemoteException {
            return false;
        }

        @Override
        public byte[] getHashFactor(String currentCredential, int userId) throws RemoteException {
            return new byte[0];
        }

        @Override
        public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) throws RemoteException {

        }

        @Override
        public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
            return false;
        }

        @Override
        public void registerStrongAuthTracker(IBinder tracker) throws RemoteException {

        }

        @Override
        public void unregisterStrongAuthTracker(IBinder tracker) throws RemoteException {

        }

        @Override
        public void requireStrongAuth(int strongAuthReason, int userId) throws RemoteException {

        }

        @Override
        public void systemReady() throws RemoteException {

        }

        @Override
        public void userPresent(int userId) throws RemoteException {

        }

        @Override
        public int getStrongAuthForUser(int userId) throws RemoteException {
            return 0;
        }

        @Override
        public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {

        }

        @Override
        public String generateKey(String alias) throws RemoteException {
            return null;
        }

        @Override
        public String importKey(String alias, byte[] keyBytes) throws RemoteException {
            return null;
        }

        @Override
        public String getKey(String alias) throws RemoteException {
            return null;
        }

        @Override
        public void removeKey(String alias) throws RemoteException {

        }

        @Override
        public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws RemoteException {

        }

        @Override
        public void setServerParams(byte[] serverParams) throws RemoteException {

        }

        @Override
        public void setRecoveryStatus(String alias, int status) throws RemoteException {

        }

        @Override
        public Map getRecoveryStatus() throws RemoteException {
            return null;
        }

        @Override
        public void closeSession(String sessionId) throws RemoteException {

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
