package com.lody.virtual.server.pm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.secondary.GmsSupport;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.collection.IntArray;
import com.lody.virtual.helper.compat.NativeLibraryHelperCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;
import com.lody.virtual.server.IAppManager;
import com.lody.virtual.server.accounts.VAccountManagerService;
import com.lody.virtual.server.am.BroadcastSystem;
import com.lody.virtual.server.am.UidSystem;
import com.lody.virtual.server.am.VActivityManagerService;
import com.lody.virtual.server.interfaces.IAppRequestListener;
import com.lody.virtual.server.interfaces.IPackageObserver;
import com.lody.virtual.server.pm.parser.PackageParserEx;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.console;
import static java.lang.System.exit;
import static mirror.android.app.ActivityThread.ActivityClientRecord.activity;

/**
 * @author Lody
 */
public class VAppManagerService extends IAppManager.Stub {

    private static final String TAG = VAppManagerService.class.getSimpleName();
    private static final AtomicReference<VAppManagerService> sService = new AtomicReference<>();
    private final UidSystem mUidSystem = new UidSystem();
    private final PackagePersistenceLayer mPersistenceLayer = new PackagePersistenceLayer(this);
    private final Set<String> mVisibleOutsidePackages = new HashSet<>();
    private boolean mBooting;
    private RemoteCallbackList<IPackageObserver> mRemoteCallbackList = new RemoteCallbackList<>();

    private IAppRequestListener mAppRequestListener;

    public static VAppManagerService get() {
        return sService.get();
    }

    public static void systemReady() {
        VEnvironment.systemReady();
        VAppManagerService instance = new VAppManagerService();
        instance.mUidSystem.initUidList();
        sService.set(instance);
    }

    public boolean isBooting() {
        return mBooting;
    }

    @Override
    public void scanApps() {
        if (mBooting) {
            return;
        }
        VLog.logbug(TAG, "=======scanApps========");
        synchronized (this) {
            mBooting = true;
            File file = VEnvironment.getPackageListFile();
            if (!file.exists()) {
                VLog.logbug(TAG, "not found package list file. Recover!");
                recover();
            } else {
                if(!mPersistenceLayer.read()) {
                    VLog.logbug(TAG, "Failed to parse package list file. Recover!");
                    recover();
                }
            }
            if (StubManifest.ENABLE_GMS ) {
                GmsSupport.installGms(0);
            }
            mBooting = false;
        }
        upgradeApps();
        VLog.d(TAG, "=======after scanApps========");
    }

    private String  needUpgrade(String packageName){
        try {
            PackageInfo vinfo = VPackageManager.get().getPackageInfo(packageName, 0 ,0);
            PackageInfo info = VirtualCore.get().getUnHookPackageManager().getPackageInfo(packageName,0);
            if (vinfo == null || info == null) {
                return null;
            }
            return vinfo.versionCode != info.versionCode? info.applicationInfo.sourceDir: null;
        }catch (Exception e) {
            VLog.e(TAG, e);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private void upgradeApps() {
        for(String s: PackageCacheManager.PACKAGE_CACHE.keySet()) {
            String newPath = needUpgrade(s);
            if(newPath != null) {
                upgradePackage(s, newPath, InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                VLog.logbug(TAG, "upgraded package: " + s + " on path:"+newPath);
            }
        }
    }

    private void recover() {
        boolean hasGsf = false;
        for (File appDir : VEnvironment.getDataAppDirectory().listFiles()) {
            String pkgName = appDir.getName();
            if ("android".equals(pkgName) || "system".equals(pkgName)) {
                continue;
            }
            if (!StubManifest.ENABLE_GMS && GmsSupport.isGmsFamilyPackage(pkgName)) {
                continue;
            }
            File storeFile = new File(appDir, "base.apk");
            int flags = 0;
            VLog.d(TAG, "recover " + appDir + "/base.apk");
            if (!storeFile.exists()) {
                ApplicationInfo appInfo = null;
                try {
                    appInfo = VirtualCore.get().getUnHookPackageManager()
                            .getApplicationInfo(pkgName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore
                }
                if (appInfo == null || appInfo.publicSourceDir == null) {
                    FileUtils.deleteDir(appDir);
                    for (int userId : VUserManagerService.get().getUserIds()) {
                        FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, pkgName));
                    }
                    continue;
                }
                storeFile = new File(appInfo.publicSourceDir);
                flags |= InstallStrategy.DEPEND_SYSTEM_IF_EXIST;
            }
            if (pkgName.equals(GmsSupport.GSF_PKG)) {
                hasGsf = true;
            }
            if (GmsSupport.hasDexFile(storeFile.getPath())) {
                InstallResult res = installPackage(pkgName, storeFile.getPath(), flags, false);
                if (!res.isSuccess) {
                    VLog.e(TAG, "Unable to install app %s: %s.", pkgName, res.error);
                    FileUtils.deleteDir(appDir);
                }
            }
        }
        //hasGSF, in case recover for new installation
        if (hasGsf && (!VirtualCore.get().isAppInstalled(GmsSupport.GSF_PKG))) {
            GmsSupport.removeGmsPackage(GmsSupport.GSF_PKG);
        }
    }

    private void cleanUpResidualFiles(PackageSetting ps) {
        File dataAppDir = VEnvironment.getDataAppPackageDirectory(ps.packageName);
        FileUtils.deleteDir(dataAppDir);
        for (int userId : VUserManagerService.get().getUserIds()) {
            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, ps.packageName));
        }
    }


    synchronized void loadPackage(PackageSetting setting) {
        if (!loadPackageInnerLocked(setting)) {
            cleanUpResidualFiles(setting);
        }
    }

    private boolean loadPackageInnerLocked(PackageSetting ps) {
        VLog.logbug(TAG, "Load package: " + ps.packageName + " id " + ps.appId);
        if (ps.dependSystem) {
            if (!VirtualCore.get().isOutsideInstalled(ps.packageName)) {
                return false;
            }
        }
        File cacheFile = VEnvironment.getPackageCacheFile(ps.packageName);
        VPackage pkg = null;
        try {
            pkg = PackageParserEx.readPackageCache(ps.packageName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (pkg == null || pkg.packageName == null) {
            return false;
        }
        chmodPackageDictionary(cacheFile);
        PackageCacheManager.put(pkg, ps);
        BroadcastSystem.get().startApp(pkg);
        return true;
    }

    @Override
    public boolean isOutsidePackageVisible(String pkg) {
        return pkg != null && mVisibleOutsidePackages.contains(pkg);
    }

    @Override
    public void addVisibleOutsidePackage(String pkg) {
        if (pkg != null) {
            mVisibleOutsidePackages.add(pkg);
        }
    }

    @Override
    public void removeVisibleOutsidePackage(String pkg) {
        if (pkg != null) {
            mVisibleOutsidePackages.remove(pkg);
        }
    }

    @Override
    public InstallResult installPackage(String pkg, String path, int flags) {
        return installPackage(pkg, path, flags, true);
    }

    @Override
    public InstallResult upgradePackage(String pkg, String path, int flags) {
        return installPackage(pkg, path, flags, false);
    }

    public synchronized InstallResult installPackage(String name, String path, int flags, boolean notify) {
        VLog.d(TAG, "install: " + path + " flags: " + flags + " notify: " + notify);
        long installTime = System.currentTimeMillis();
        if (path == null) {
            return InstallResult.makeFailure("path = NULL");
        }
        boolean artFlyMode = VirtualRuntime.isArt() && (flags & InstallStrategy.ART_FLY_MODE) != 0;
        File packageFile = new File(path);
        if (!packageFile.exists() || !packageFile.isFile()) {
            return InstallResult.makeFailure("Package File is not exist.");
        }
        VPackage pkg = null;
        try {
            pkg = PackageParserEx.parsePackage(packageFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (pkg == null || pkg.packageName == null) {
            return InstallResult.makeFailure("Unable to parse the package.");
        }
        VLog.logbug(TAG, "install package: " + pkg.packageName + " path: " + path);
        InstallResult res = new InstallResult();
        res.packageName = pkg.packageName;
        // PackageCache holds all packages, try to check if we need to update.
        VPackage existOne = PackageCacheManager.get(pkg.packageName);
        PackageSetting existSetting = existOne != null ? (PackageSetting) existOne.mExtras : null;
        if (existOne != null) {
            if ((flags & InstallStrategy.IGNORE_NEW_VERSION) != 0) {
                res.isUpdate = true;
                res.isSuccess = true;
                return res;
            }
            if (!canUpdate(existOne, pkg, flags)) {
                return InstallResult.makeFailure("Not allowed to update the package.");
            }
            res.isUpdate = true;
        }
        File appDir = VEnvironment.getDataAppPackageDirectory(pkg.packageName);


        File libDir = new File(appDir, "lib");
        if (res.isUpdate) {
            FileUtils.deleteDir(libDir);
            VEnvironment.getOdexFile(pkg.packageName).delete();
            VActivityManagerService.get().killAppByPkg(pkg.packageName, VUserHandle.USER_ALL);
        }
        if (!libDir.exists() && !libDir.mkdirs()) {
            return InstallResult.makeFailure("Unable to create lib dir.");
        }
        boolean dependSystem = (flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0
                && VirtualCore.get().isOutsideInstalled(pkg.packageName);

        if (existSetting != null && !existSetting.dependSystem) {
            dependSystem = false;
        }
        VLog.d(TAG, "dependSystem " + dependSystem + " set : " + ((flags & InstallStrategy.DEPEND_SYSTEM_IF_EXIST) != 0));

        NativeLibraryHelperCompat.copyNativeBinaries(new File(path), libDir);
        if (!dependSystem) {
            File privatePackageFile = new File(appDir, "base.apk");
            File parentFolder = privatePackageFile.getParentFile();
            if (!parentFolder.exists() && !parentFolder.mkdirs()) {
                VLog.w(TAG, "Warning: unable to create folder : " + privatePackageFile.getPath());
            } else if (privatePackageFile.exists() && !privatePackageFile.delete()) {
                VLog.w(TAG, "Warning: unable to delete file : " + privatePackageFile.getPath());
            }
            try {
                FileUtils.copyFile(packageFile, privatePackageFile);
            } catch (IOException e) {
                privatePackageFile.delete();
                return InstallResult.makeFailure("Unable to copy the package file.");
            }
            packageFile = privatePackageFile;
        }
        if (existOne != null) {
            PackageCacheManager.remove(pkg.packageName);
        }
        chmodPackageDictionary(packageFile);
        PackageSetting ps;
        if (existSetting != null) {
            ps = existSetting;
        } else {
            ps = new PackageSetting();
        }
        ps.artFlyMode = artFlyMode;
        ps.dependSystem = dependSystem;
        ps.apkPath = packageFile.getPath();
        ps.libPath = libDir.getPath();
        ps.packageName = pkg.packageName;
        ps.appId = VUserHandle.getAppId(mUidSystem.getOrCreateUid(pkg));
        if (res.isUpdate) {
            ps.lastUpdateTime = installTime;
        } else {
            ps.firstInstallTime = installTime;
            ps.lastUpdateTime = installTime;
            for (int userId : VUserManagerService.get().getUserIds()) {
                boolean installed = userId == 0;
                ps.setUserState(userId, false/*launched*/, false/*hidden*/, installed);
            }
        }
        PackageParserEx.savePackageCache(pkg);
        PackageCacheManager.put(pkg, ps);
        mPersistenceLayer.save();
        BroadcastSystem.get().startApp(pkg);
        if (notify) {
            notifyAppInstalled(ps, -1);
        }
        res.isSuccess = true;
        return res;
    }


    @Override
    public synchronized boolean installPackageAsUser(int userId, String packageName) {
        if (VUserManagerService.get().exists(userId)) {
            PackageSetting ps = PackageCacheManager.getSetting(packageName);
            if (ps != null) {
                if (!ps.isInstalled(userId)) {
                    ps.setInstalled(userId, true);
                    notifyAppInstalled(ps, userId);
                    mPersistenceLayer.save();
                    return true;
                }
            }
        }
        return false;
    }

    private void chmodPackageDictionary(File packageFile) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (FileUtils.isSymlink(packageFile)) {
                    return;
                }
                FileUtils.chmod(packageFile.getParentFile().getAbsolutePath(), FileUtils.FileMode.MODE_755);
                FileUtils.chmod(packageFile.getAbsolutePath(), FileUtils.FileMode.MODE_755);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean canUpdate(VPackage existOne, VPackage newOne, int flags) {
        if ((flags & InstallStrategy.COMPARE_VERSION) != 0) {
            if (existOne.mVersionCode < newOne.mVersionCode) {
                return true;
            }
        }
        if ((flags & InstallStrategy.TERMINATE_IF_EXIST) != 0) {
            return false;
        }
        if ((flags & InstallStrategy.UPDATE_IF_EXIST) != 0) {
            return true;
        }
        return false;
    }


    @Override
    public synchronized boolean uninstallPackage(String packageName) {
        VLog.logbug(TAG, "uninstallPackage " + packageName);
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            uninstallPackageFully(ps);
            return true;
        }
        return false;
    }
    @Override
    public synchronized boolean uninstallPackageAsUser(String packageName, int userId) {
        if (!VUserManagerService.get().exists(userId)) {
            return false;
        }
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            int[] userIds = getPackageInstalledUsers(packageName);
            if (!ArrayUtils.contains(userIds, userId)) {
                return false;
            }
            if (userIds.length == 1) {
                uninstallPackageFully(ps);
            } else {
                VActivityManagerService.get().killAppByPkg(packageName, userId);
                ps.setInstalled(userId, false);
                notifyAppUninstalled(ps, userId);
                mPersistenceLayer.save();
                FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(userId, packageName));
            }
            return true;
        }
        return false;
    }
    private void uninstallPackageFully(PackageSetting ps) {
        String packageName = ps.packageName;
                    try {
                        BroadcastSystem.get().stopApp(packageName);
                        VActivityManagerService.get().killAppByPkg(packageName, VUserHandle.USER_ALL);
                        VEnvironment.getPackageResourcePath(packageName).delete();
                        FileUtils.deleteDir(VEnvironment.getDataAppPackageDirectory(packageName));
                        VEnvironment.getOdexFile(packageName).delete();
                        for (int id : VUserManagerService.get().getUserIds()) {
                            FileUtils.deleteDir(VEnvironment.getDataUserPackageDirectory(id, packageName));
                        }
                        PackageCacheManager.remove(packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
            notifyAppUninstalled(ps, -1);
        }
    }

    @Override
    public int[] getPackageInstalledUsers(String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null) {
            IntArray installedUsers = new IntArray(5);
            int[] userIds = VUserManagerService.get().getUserIds();
            for (int userId : userIds) {
                if (ps.readUserState(userId).installed) {
                    installedUsers.add(userId);
                }
            }
            return installedUsers.getAll();
        }
        return new int[0];
    }

    @Override
    public List<InstalledAppInfo> getInstalledApps(int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            infoList.add(setting.getAppInfo());
        }
        return infoList;
    }

    @Override
    public List<InstalledAppInfo> getInstalledAppsAsUser(int userId, int flags) {
        List<InstalledAppInfo> infoList = new ArrayList<>(getInstalledAppCount());
        for (VPackage p : PackageCacheManager.PACKAGE_CACHE.values()) {
            PackageSetting setting = (PackageSetting) p.mExtras;
            boolean visible = setting.isInstalled(userId);
            if ((flags & VirtualCore.GET_HIDDEN_APP) == 0 && setting.isHidden(userId)) {
                visible = false;
            }
            if (visible) {
                infoList.add(setting.getAppInfo());
            }
        }
        return infoList;
    }

    @Override
    public int getInstalledAppCount() {
        return PackageCacheManager.PACKAGE_CACHE.size();
    }

    @Override
    public boolean isAppInstalled(String packageName) {
        return packageName != null && PackageCacheManager.PACKAGE_CACHE.containsKey(packageName);
    }

    @Override
    public boolean isAppInstalledAsUser(int userId, String packageName) {
        if (packageName == null || !VUserManagerService.get().exists(userId)) {
            return false;
        }
        PackageSetting setting = PackageCacheManager.getSetting(packageName);
        if (setting == null) {
            return false;
        }
        return setting.isInstalled(userId);
    }

    private void notifyAppInstalled(PackageSetting setting, int userId) {
        final String pkg = setting.packageName;
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                if (userId == -1) {
                    sendInstalledBroadcast(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalled(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalledAsUser(0, pkg);
                } else {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageInstalledAsUser(userId, pkg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mRemoteCallbackList.finishBroadcast();
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }

    private void notifyAppUninstalled(PackageSetting setting, int userId) {
        final String pkg = setting.packageName;
        int N = mRemoteCallbackList.beginBroadcast();
        while (N-- > 0) {
            try {
                if (userId == -1) {
                    sendUninstalledBroadcast(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalled(pkg);
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalledAsUser(0, pkg);
                } else {
                    mRemoteCallbackList.getBroadcastItem(N).onPackageUninstalledAsUser(userId, pkg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mRemoteCallbackList.finishBroadcast();
        VAccountManagerService.get().refreshAuthenticatorCache(null);
    }

    public void sendBootCompleted() {
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        VLog.logbug(TAG, "sendBootCompleted intent");
        VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.ALL);
    }

    private void sendInstalledBroadcast(String packageName) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_ADDED);
        intent.setData(Uri.parse("package:" + packageName));
        VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.ALL);
    }
    private void sendUninstalledBroadcast(String packageName) {
        Intent intent = new Intent(Intent.ACTION_PACKAGE_REMOVED);
        intent.setData(Uri.parse("package:" + packageName));
        VActivityManagerService.get().sendBroadcastAsUser(intent, VUserHandle.ALL);
    }
    @Override
    public void registerObserver(IPackageObserver observer) {
        try {
            mRemoteCallbackList.register(observer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregisterObserver(IPackageObserver observer) {
        try {
            mRemoteCallbackList.unregister(observer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public IAppRequestListener getAppRequestListener() {
        return mAppRequestListener;
    }

    @Override
    public void setAppRequestListener(final IAppRequestListener listener) {
        this.mAppRequestListener = listener;
        if (listener != null) {
            try {
                listener.asBinder().linkToDeath(new DeathRecipient() {
                    @Override
                    public void binderDied() {
                        listener.asBinder().unlinkToDeath(this, 0);
                        VAppManagerService.this.mAppRequestListener = null;
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void clearAppRequestListener() {
        this.mAppRequestListener = null;
    }

    @Override
    public InstalledAppInfo getInstalledAppInfo(String packageName, int flags) {
        synchronized (PackageCacheManager.class) {
            if (packageName != null) {
                PackageSetting setting = PackageCacheManager.getSetting(packageName);
                if (setting != null) {
                    return setting.getAppInfo();
                }
            }
            return null;
        }
    }

    public boolean isPackageLaunched(int userId, String packageName) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        return ps != null && ps.isLaunched(userId);
    }

    public void setPackageHidden(int userId, String packageName, boolean hidden) {
        PackageSetting ps = PackageCacheManager.getSetting(packageName);
        if (ps != null && VUserManagerService.get().exists(userId)) {
            ps.setHidden(userId, hidden);
            mPersistenceLayer.save();
        }
    }

    public int getAppId(String packageName) {
        PackageSetting setting = PackageCacheManager.getSetting(packageName);
        return setting != null ? setting.appId : -1;
    }


    void restoreFactoryState() {
        VLog.logbug(TAG, "Warning: Restore the factory state...");
        VLog.keyLog(VirtualCore.get().getContext(), TAG, "Factory Reset");
        VEnvironment.getDalvikCacheDirectory().delete();
        VEnvironment.getUserSystemDirectory().delete();
        VEnvironment.getDataAppDirectory().delete();
    }

    public void savePersistenceData() {
        mPersistenceLayer.save();
    }

    public void notifyActivityBeforeResume(String pkg){
        VLog.d(TAG, "notifyActivityBeforeResume " + pkg);
        VirtualCore.get().getComponentDelegate().beforeActivityResume(pkg);
    }
    public void  notifyActivityBeforePause(String pkg){
        VLog.d(TAG, "notifyActivityBeforePause " + pkg);
        VirtualCore.get().getComponentDelegate().beforeActivityPause(pkg);
    }
    public void  reloadLockerSetting(String key, boolean adFree, long interval){
        VLog.d(TAG, "reloadLockerSetting ");
        VirtualCore.get().getComponentDelegate().reloadLockerSetting(key, adFree, interval);
    }

    public void restart() {
        VLog.logbug(TAG, "restart...");
        VirtualCore.get().killAllApps();
        mPersistenceLayer.save();
        try{
            Thread.sleep(300);
        }catch (Throwable e) {

        }
        VLog.logbug(TAG, "stopping...");
        exit(0);
    }
}
