package com.lody.virtual.client.hook.secondary;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.server.pm.VAppManagerService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Lody
 */
public class GmsSupport {

    private static final HashSet<String> GOOGLE_APP = new HashSet<>();
    static {
        GOOGLE_APP.add("com.android.vending");
        GOOGLE_APP.add("com.google.android.play.games");
        GOOGLE_APP.add("com.google.android.wearable.app");
        GOOGLE_APP.add("com.google.android.wearable.app.cn");
    }

    private static final HashSet<String> GOOGLE_SERVICE =  new HashSet<>();
    static {
        GOOGLE_SERVICE.add("com.google.android.gsf");
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
            if (userId == 0) {
                service.installPackage(info.sourceDir, InstallStrategy.DEPEND_SYSTEM_IF_EXIST, false);
            } else {
                service.installPackageAsUser(userId, packageName);
            }
        }
    }

    public static void installGms(int userId) {
        installPackages(GOOGLE_SERVICE, userId);
        installPackages(GOOGLE_APP, userId);
    }
}
