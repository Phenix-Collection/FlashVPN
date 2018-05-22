// ICloneAgent.aidl
package com.polestar.clone;

// Declare any non-default types here with import statements
interface ICloneAgent {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void createClone(String pkg, int userId);
    void deleteClone(String pkg, int userId);
    void launchApp(String pkg, int userId);
    boolean isNeedUpgrade(String pkg);
    void upgradeApp(String pkg);
    boolean isCloned(String pkg, int userId);
}

