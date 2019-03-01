// ILockSettings.aidl
package com.android.internal.widget;
// Declare any non-default types here with import statements

interface ILockSettings {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void setRecoverySecretTypes(in int[] secretTypes);
    int[] getRecoverySecretTypes();

    void setLong(in String key, in long value, in int userId);
    void setString(in String key, in String value, in int userId);
    boolean getBoolean(in String key, in boolean defaultValue, in int userId);
    long getLong(in String key, in long defaultValue, in int userId);
    String getString(in String key, in String defaultValue, in int userId);
    void setLockCredential(in String credential, int type, in String savedCredential, int requestedQuality, int userId);
    void resetKeyStore(int userId);
//    VerifyCredentialResponse checkCredential(in String credential, int type, int userId,
//            in ICheckCredentialProgressCallback progressCallback);
//    VerifyCredentialResponse verifyCredential(in String credential, int type, long challenge, int userId);
//    VerifyCredentialResponse verifyTiedProfileChallenge(String credential, int type, long challenge, int userId);
    boolean checkVoldPassword(int userId);
    boolean havePattern(int userId);
    boolean havePassword(int userId);
    byte[] getHashFactor(String currentCredential, int userId);
    void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword);
    boolean getSeparateProfileChallengeEnabled(int userId);
    void registerStrongAuthTracker(in IBinder tracker);
    void unregisterStrongAuthTracker(in IBinder tracker);
    void requireStrongAuth(int strongAuthReason, int userId);
    void systemReady();
    void userPresent(int userId);
    int getStrongAuthForUser(int userId);

    // Keystore RecoveryController methods.
    // {@code ServiceSpecificException} may be thrown to signal an error, which caller can
    // convert to  {@code RecoveryManagerException}.
    void initRecoveryServiceWithSigFile(in String rootCertificateAlias,
            in byte[] recoveryServiceCertFile, in byte[] recoveryServiceSigFile);
//    KeyChainSnapshot getKeyChainSnapshot();
    String generateKey(String alias);
    String importKey(String alias, in byte[] keyBytes);
    String getKey(String alias);
    void removeKey(String alias);
    void setSnapshotCreatedPendingIntent(in PendingIntent intent);
    void setServerParams(in byte[] serverParams);
    void setRecoveryStatus(in String alias, int status);
    Map getRecoveryStatus();
//    void setRecoverySecretTypes(in int[] secretTypes);
//    int[] getRecoverySecretTypes();
//    byte[] startRecoverySessionWithCertPath(in String sessionId, in String rootCertificateAlias,
//            in RecoveryCertPath verifierCertPath, in byte[] vaultParams, in byte[] vaultChallenge,
//            in List<KeyChainProtectionParams> secrets);
//    Map/*<String, String>*/ recoverKeyChainSnapshot(
//            in String sessionId,
//            in byte[] recoveryKeyBlob,
//            in List<WrappedApplicationKey> applicationKeys);
    void closeSession(in String sessionId);
}
