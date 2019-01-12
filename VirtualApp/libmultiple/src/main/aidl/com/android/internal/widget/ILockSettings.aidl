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
}
