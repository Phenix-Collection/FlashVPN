package com.lody.virtual.client.hook.secondary;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.pm.VAppManagerService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * @author Lody
 */
public class GmsSupport {

    private static final HashSet<String> GOOGLE_APP = new HashSet<>();
    private static final String TAG = "GmsSupport";
    public static final String GSF_PKG = "com.google.android.gsf";
    static {
        GOOGLE_APP.add("com.android.vending");
        GOOGLE_APP.add("com.google.android.play.games");
        GOOGLE_APP.add("com.google.android.wearable.app");
        GOOGLE_APP.add("com.google.android.wearable.app.cn");
    }

    private static HashSet<String> GOOGLE_SERVICE =  new HashSet<>();
    static {
        GOOGLE_SERVICE.add(GSF_PKG);
        GOOGLE_SERVICE.add("com.google.android.gms");
        GOOGLE_SERVICE.add("com.google.android.gsf.login");
        GOOGLE_SERVICE.add("com.google.android.backuptransport");
        GOOGLE_SERVICE.add("com.google.android.backup");
        GOOGLE_SERVICE.add("com.google.android.configupdater");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.contacts");
        GOOGLE_SERVICE.add("com.google.android.feedback");
        GOOGLE_SERVICE.add("com.google.android.onetimeinitializer");
        GOOGLE_SERVICE.add("com.google.android.partnersetup");
        GOOGLE_SERVICE.add("com.google.android.setupwizard");
        GOOGLE_SERVICE.add("com.google.android.syncadapters.calendar");
    };

    //Make the outside gms package visible
    public static void removeGmsPackage(String pkg) {
        GOOGLE_APP.remove(pkg);
        GOOGLE_SERVICE.remove(pkg);
    }

    public static boolean hasDexFile( String apkPath) {
        if (apkPath == null) {
            return  false;
        }
        if ( apkPath.contains("/system/app") || apkPath.startsWith("/system/priv-app")) {
            boolean hasDex = false;
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(apkPath);
                hasDex = zipFile.getEntry("classes.dex") != null;
                zipFile.close();
            }catch (Throwable e) {
                VLog.logbug(TAG, "Error when find dex for path: " + apkPath);
                VLog.logbug(TAG, VLog.getStackTraceString(e));
            }
            VLog.logbug(TAG, "apk : " + apkPath + " hasDex: " + hasDex);
            return hasDex;
        }
        return true;
    }


    public static boolean isGmsFamilyPackage(String packageName) {
//        return packageName.equals("com.android.vending")
//                || packageName.equals("com.google.android.gms");
        return GOOGLE_SERVICE.contains(packageName) || GOOGLE_APP.contains(packageName);
    }

    public static boolean isGoogleFrameworkInstalled() {
        return VirtualCore.get().isAppInstalled("com.google.android.gms");
    }

    private static void installPackages(Set<String> list, int userId) {
        VAppManagerService service = VAppManagerService.get();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String packageName = (String) iterator.next();
            if (service.isAppInstalledAsUser(userId, packageName)) {
                continue;
            }
            ApplicationInfo info = null;
            try {
                info = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
            if (info == null || info.sourceDir == null) {
                continue;
            }
            if (hasDexFile(info.sourceDir)) {
                if (userId == 0) {
                    service.installPackage(packageName, info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST, false);
                } else {
                    service.installPackageAsUser(userId, packageName);
                }
            }
        }
    }

    public static void installGms(int userId) {
        installPackages(GOOGLE_SERVICE, userId);
        installPackages(GOOGLE_APP, userId);
        if (!VirtualCore.get().isAppInstalled(GSF_PKG)) {
            removeGmsPackage(GSF_PKG);
        }
    }
}
