// IAppMonitor.aidl
package mochat.multiple.parallel.whatsclone;

// Declare any non-default types here with import statements

interface IAppMonitor {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onAppSwitchForeground(String pkg, int userId);
    void onAppSwitchBackground(String pkg, int userId);
    void onAppLock(String pkg, int userId);
    void onAdsLaunch(String pkg, int userId, String name);
}