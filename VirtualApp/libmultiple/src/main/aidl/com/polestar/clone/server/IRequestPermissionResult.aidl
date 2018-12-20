// IRequestPermissionResult.aidl
package com.polestar.clone.server;

// Declare any non-default types here with import statements

interface IRequestPermissionResult {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean onResult(int arg1, in String[] arg2, in int[] arg3);
}
