package com.polestar.clone.server.pm.parser;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Base64;

import com.polestar.clone.CustomizeAppData;
import com.polestar.clone.GmsSupport;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.env.Constants;
import com.polestar.clone.client.fixer.ComponentFixer;
import com.polestar.clone.helper.collection.ArrayMap;
import com.polestar.clone.helper.compat.BuildCompat;
import com.polestar.clone.helper.compat.PackageParserCompat;
import com.polestar.clone.helper.utils.FileUtils;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VEnvironment;
import com.polestar.clone.server.pm.PackageCacheManager;
import com.polestar.clone.server.pm.PackageSetting;
import com.polestar.clone.server.pm.PackageUserState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mirror.android.content.pm.ApplicationInfoL;
import mirror.android.content.pm.ApplicationInfoN;
import mirror.dalvik.system.VMRuntime;

/**
 * @author Lody
 */

public class PackageParserEx {

    private static final String TAG = PackageParserEx.class.getSimpleName();

    private static final ArrayMap<String, String[]> sSharedLibCache = new ArrayMap<>();
//    public static String hackCallingPackage = null;

    public static VPackage parsePackage(File packageFile) throws Throwable {
        PackageParser parser = PackageParserCompat.createParser(packageFile);
        PackageParser.Package p = PackageParserCompat.parsePackage(parser, packageFile, 0);
        if (p.requestedPermissions.contains("android.permission.FAKE_PACKAGE_SIGNATURE")
                && p.mAppMetaData != null
                    && p.mAppMetaData.containsKey(Constants.FEATURE_FAKE_SIGNATURE)) {
            String sig = p.mAppMetaData.getString(Constants.FEATURE_FAKE_SIGNATURE);
            p.mSignatures = new Signature[]{new Signature(sig)};
            VLog.logbug(TAG, "Using fake-signature feature on : " + p.packageName);
        } else {
            PackageParserCompat.collectCertificates(parser, p, PackageParser.PARSE_IS_SYSTEM);
        }
        return buildPackageCache(p);
    }

    public static VPackage readPackageCache(String packageName) {
        Parcel p = Parcel.obtain();
        try {
            File cacheFile = VEnvironment.getPackageCacheFile(packageName);
            FileInputStream is = new FileInputStream(cacheFile);
            byte[] bytes = FileUtils.toByteArray(is);
            is.close();
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            if (p.readInt() != 4) {
                throw new IllegalStateException("Invalid version.");
            }
            VPackage pkg = new VPackage(p);
            addOwner(pkg);
            return pkg;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        return null;
    }

    public static void readSignature(VPackage pkg) {
        File signatureFile = VEnvironment.getSignatureFile(pkg.packageName);
        if (!signatureFile.exists()) {
            return;
        }
        Parcel p = Parcel.obtain();
        try {
            FileInputStream fis = new FileInputStream(signatureFile);
            byte[] bytes = FileUtils.toByteArray(fis);
            fis.close();
            p.unmarshall(bytes, 0, bytes.length);
            p.setDataPosition(0);
            pkg.mSignatures = p.createTypedArray(Signature.CREATOR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
    }

    public static void savePackageCache(VPackage pkg) {
        final String packageName = pkg.packageName;
        Parcel p = Parcel.obtain();
        try {
            p.writeInt(4);
            pkg.writeToParcel(p, 0);
            FileOutputStream fos = new FileOutputStream(VEnvironment.getPackageCacheFile(packageName));
            fos.write(p.marshall());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            p.recycle();
        }
        Signature[] signatures = pkg.mSignatures;
        if (signatures != null) {
            File signatureFile = VEnvironment.getSignatureFile(packageName);
            if (signatureFile.exists() && !signatureFile.delete()) {
                VLog.w(TAG, "Unable to delete the signatures of " + packageName);
            }
            p = Parcel.obtain();
            try {
                p.writeTypedArray(signatures, 0);
                FileUtils.writeParcelToFile(p, signatureFile);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                p.recycle();
            }
        }
    }

    private static Signature[] getSignature(PackageParser.Package arg1) {
        Signature[] v0 = BuildCompat.isPie() ? arg1.mSigningDetails.signatures : arg1.mSignatures;
        return v0;
    }

    private static VPackage buildPackageCache(PackageParser.Package p) {
        VPackage cache = new VPackage();
        cache.activities = new ArrayList<>(p.activities.size());
        cache.services = new ArrayList<>(p.services.size());
        cache.receivers = new ArrayList<>(p.receivers.size());
        cache.providers = new ArrayList<>(p.providers.size());
        cache.instrumentation = new ArrayList<>(p.instrumentation.size());
        cache.permissions = new ArrayList<>(p.permissions.size());
        cache.permissionGroups = new ArrayList<>(p.permissionGroups.size());

        for (PackageParser.Activity activity : p.activities) {
            cache.activities.add(new VPackage.ActivityComponent(activity));
        }
        for (PackageParser.Service service : p.services) {
            if (!service.getComponentName().getClassName().
                    equals("com.google.android.gms.phenotype.service.sync.PackageUpdateTaskService")
                    ) {
                cache.services.add(new VPackage.ServiceComponent(service));
            }
        }
        for (PackageParser.Activity receiver : p.receivers) {
            cache.receivers.add(new VPackage.ActivityComponent(receiver));
        }
        for (PackageParser.Provider provider : p.providers) {
            cache.providers.add(new VPackage.ProviderComponent(provider));
        }
        for (PackageParser.Instrumentation instrumentation : p.instrumentation) {
            cache.instrumentation.add(new VPackage.InstrumentationComponent(instrumentation));
        }
        cache.requestedPermissions = new ArrayList<>(p.requestedPermissions.size());
        cache.requestedPermissions.addAll(p.requestedPermissions);
        if (mirror.android.content.pm.PackageParser.Package.protectedBroadcasts != null) {
            List<String> protectedBroadcasts = mirror.android.content.pm.PackageParser.Package.protectedBroadcasts.get(p);
            if (protectedBroadcasts != null) {
                cache.protectedBroadcasts = new ArrayList<>(protectedBroadcasts);
                cache.protectedBroadcasts.addAll(protectedBroadcasts);
            }
        }
        cache.applicationInfo = p.applicationInfo;
        cache.mSignatures = getSignature(p);
        cache.mAppMetaData = p.mAppMetaData;
        cache.packageName = p.packageName;
        cache.mPreferredOrder = p.mPreferredOrder;
        cache.mVersionName = p.mVersionName;
        cache.mSharedUserId = p.mSharedUserId;
        cache.mSharedUserLabel = p.mSharedUserLabel;
        cache.usesLibraries = p.usesLibraries;
        cache.mVersionCode = p.mVersionCode;
        cache.configPreferences = p.configPreferences;
        cache.reqFeatures = p.reqFeatures;
        addOwner(cache);
        return cache;
    }
//
//
//    public static void initApplicationInfoBase(PackageSetting arg5, VPackage arg6) {
//        ApplicationInfo v1 = arg6.applicationInfo;
//        if(TextUtils.isEmpty(v1.processName)) {
//            v1.processName = v1.packageName;
//        }
//
//        v1.enabled = true;
//        v1.uid = arg5.appId;
//        v1.name = ComponentFixer.fixComponentClassName(arg5.packageName, v1.name);
//        if(Build$VERSION.SDK_INT >= 21) {
//            ApplicationInfoL.scanSourceDir.set(v1, v1.dataDir);
//            ApplicationInfoL.scanPublicSourceDir.set(v1, v1.dataDir);
//            ApplicationInfoL.primaryCpuAbi.set(v1, ApplicationInfoL.primaryCpuAbi.get(VirtualCore.get().getContext().getApplicationInfo()));
//        }
//
//        if(arg5.appMode == 1) {
//            Object v0 = PackageParserEx.sSharedLibCache.get(arg5.packageName);
//            if(v0 == null) {
//                PackageManager v2 = VirtualCore.get().getUnHookPackageManager();
//                try {
//                    String[] v0_1 = v2.getApplicationInfo(arg5.packageName, 1024).sharedLibraryFiles;
//                    if(v0_1 == null) {
//                        v0_1 = new String[0];
//                    }
//
//                    PackageParserEx.sSharedLibCache.put(arg5.packageName, v0_1);
//                }
//                catch(PackageManager$NameNotFoundException v2_1) {
//                }
//            }
//
//            v1.sharedLibraryFiles = ((String[])v0);
//        }
//    }

    public static void initApplicationInfoBase(PackageSetting ps, VPackage p) {
        ApplicationInfo ai = p.applicationInfo;
//        ai.flags |= ApplicationInfo.FLAG_HAS_CODE;
        if (TextUtils.isEmpty(ai.processName)) {
            ai.processName = ai.packageName;
        }
        ai.enabled = true;
        ai.nativeLibraryDir = ps.libPath;
        ai.uid = ps.appId;
        ai.name = ComponentFixer.fixComponentClassName(ps.packageName, ai.name);
//        ai.publicSourceDir = ps.apkPath;
//        ai.sourceDir = ps.apkPath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ai.splitSourceDirs = new String[]{ps.apkPath};
//            ai.splitPublicSourceDirs = ai.splitSourceDirs;
            ApplicationInfoL.scanSourceDir.set(ai, ai.dataDir);
            ApplicationInfoL.scanPublicSourceDir.set(ai, ai.dataDir);
            String hostPrimaryCpuAbi = ApplicationInfoL.primaryCpuAbi.get(VirtualCore.get().getContext().getApplicationInfo());
            ApplicationInfoL.primaryCpuAbi.set(ai, hostPrimaryCpuAbi);
        }

        if (ps.dependSystem) {
            String[] sharedLibraryFiles = sSharedLibCache.get(ps.packageName);
            if (sharedLibraryFiles == null) {
                PackageManager hostPM = VirtualCore.get().getUnHookPackageManager();
                try {
                    ApplicationInfo hostInfo = hostPM.getApplicationInfo(ps.packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
                    sharedLibraryFiles = hostInfo.sharedLibraryFiles;
                    if (sharedLibraryFiles == null) sharedLibraryFiles = new String[0];
                    sSharedLibCache.put(ps.packageName, sharedLibraryFiles);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            ai.sharedLibraryFiles = sharedLibraryFiles;
        }
    }

//
//    private static void initApplicationAsUser(ApplicationInfo arg9, int arg10) {
//        String v0_1;
//        ApplicationInfo v1_1;
//        ApplicationInfo v0;
//        int v8 = 21;
//        PackageSetting v3 = PackageCacheManager.getSetting(arg9.packageName);
//        if(v3 == null) {
//            throw new IllegalStateException();
//        }
//
//        boolean v4 = v3.isRunOn64BitProcess();
//        String v5 = v3.getApkPath(v4);
//        arg9.publicSourceDir = v5;
//        arg9.sourceDir = v5;
//        AppLibConfig v2 = VirtualCore.getConfig().getAppLibConfig(arg9.packageName);
//        if(v4) {
//            if(Build$VERSION.SDK_INT >= v8) {
//                ApplicationInfoL.primaryCpuAbi.set(arg9, "arm64-v8a");
//            }
//
//            arg9.nativeLibraryDir = VEnvironment.getAppLibDirectory64(arg9.packageName).getPath();
//        }
//        else {
//            arg9.nativeLibraryDir = VEnvironment.getAppLibDirectory(arg9.packageName).getPath();
//        }
//
//        try {
//            v0 = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(arg9.packageName, 0);
//            v1_1 = v0;
//        }
//        catch(PackageManager$NameNotFoundException v1) {
//            v1_1 = v0;
//        }
//
//        if(v3.appMode == 1) {
//            if(v2 == AppLibConfig.UseRealLib && v1_1 == null) {
//                v2 = AppLibConfig.UseOwnLib;
//            }
//
//            if(v2 != AppLibConfig.UseRealLib) {
//                goto label_50;
//            }
//
//            if(v4) {
//                arg9.nativeLibraryDir = v1_1.nativeLibraryDir;
//                if(Build$VERSION.SDK_INT < v8) {
//                    goto label_50;
//                }
//
//                ApplicationInfoL.primaryCpuAbi.set(arg9, ApplicationInfoL.primaryCpuAbi.get(v1_1));
//                goto label_50;
//            }
//
//            v0_1 = PackageParserEx.choose32bitLibPath(v1_1);
//            if(v0_1 == null) {
//                goto label_50;
//            }
//
//            arg9.nativeLibraryDir = v0_1;
//        }
//
//        label_50:
//        arg9.dataDir = v4 ? VEnvironment.getDataUserPackageDirectory64(arg10, arg9.packageName).getPath() : VEnvironment.getDataUserPackageDirectory(arg10, arg9.packageName).getPath();
//        v0_1 = new File(v5).getParent();
//        if(Build$VERSION.SDK_INT >= v8) {
//            ApplicationInfoL.scanSourceDir.set(arg9, v0_1);
//            ApplicationInfoL.scanPublicSourceDir.set(arg9, v0_1);
//            if(v2 == AppLibConfig.UseRealLib && v1_1 != null) {
//                ApplicationInfoL.splitPublicSourceDirs.set(arg9, v1_1.splitPublicSourceDirs);
//                ApplicationInfoL.splitSourceDirs.set(arg9, v1_1.splitSourceDirs);
//            }
//
//            if(Build$VERSION.SDK_INT < 26) {
//                goto label_79;
//            }
//
//            if(v1_1 == null) {
//                goto label_79;
//            }
//
//            arg9.splitNames = v1_1.splitNames;
//        }
//
//        label_79:
//        if(Build$VERSION.SDK_INT >= 24) {
//            v0_1 = v4 ? VEnvironment.getDeDataUserPackageDirectory64(arg10, arg9.packageName).getPath() : VEnvironment.getDeDataUserPackageDirectory(arg10, arg9.packageName).getPath();
//            if(ApplicationInfoN.deviceEncryptedDataDir != null) {
//                ApplicationInfoN.deviceEncryptedDataDir.set(arg9, v0_1);
//            }
//
//            if(ApplicationInfoN.credentialEncryptedDataDir != null) {
//                ApplicationInfoN.credentialEncryptedDataDir.set(arg9, arg9.dataDir);
//            }
//
//            if(ApplicationInfoN.deviceProtectedDataDir != null) {
//                ApplicationInfoN.deviceProtectedDataDir.set(arg9, v0_1);
//            }
//
//            if(ApplicationInfoN.credentialProtectedDataDir == null) {
//                goto label_104;
//            }
//
//            ApplicationInfoN.credentialProtectedDataDir.set(arg9, arg9.dataDir);
//        }
//
//        label_104:
//        if((VirtualCore.getConfig().isUseRealDataDir(arg9.packageName)) && (VirtualCore.getConfig().isEnableIORedirect())) {
//            arg9.dataDir = "/data/data/" + arg9.packageName + "/";
//        }
//    }

//    private static HashMap<String, String> mLabelCache = new HashMap<>();
    private static void initApplicationAsUser(ApplicationInfo ai, int userId) {
        String v0_1;
        ApplicationInfo realAppInfo = null;
        int v8 = 21;
        PackageSetting ps = PackageCacheManager.getSetting(ai.packageName);
        if(ps == null) {
            throw new IllegalStateException();
        }

        String apkPath = ps.apkPath;
        ai.publicSourceDir = apkPath;
        ai.sourceDir = apkPath;

        ai.dataDir = VEnvironment.getDataUserPackageDirectory(userId, ai.packageName).getPath();

        try {
            realAppInfo = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(ai.packageName, 0);
        }
        catch(PackageManager.NameNotFoundException v1) {
        }

        String libDir ;
        if (ps.dependSystem && realAppInfo != null ) {
            if (VirtualCore.get().isRunningOn64bit()) {
                libDir = realAppInfo.nativeLibraryDir;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ApplicationInfoL.primaryCpuAbi.set(ai, "arm64-v8a");
                }
            } else {
                libDir = choose32bitLibPath(realAppInfo);
            }
//            VLog.d("JJJJ", "get libdir " + libDir);
            if (libDir != null) {
                ai.nativeLibraryDir = libDir;
            }

//            VLog.d("JJJJ", "Final nativeLibraryDir " + ai.nativeLibraryDir);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String baseDir = new File(apkPath).getParent();
            ApplicationInfoL.scanSourceDir.set(ai, baseDir);
            ApplicationInfoL.scanPublicSourceDir.set(ai, baseDir);
            if (ps.dependSystem && realAppInfo != null) {
                ApplicationInfoL.splitPublicSourceDirs.set(ai, realAppInfo.splitPublicSourceDirs);
                ApplicationInfoL.splitSourceDirs.set(ai, realAppInfo.splitSourceDirs);
                ai.splitPublicSourceDirs = realAppInfo.splitPublicSourceDirs;
                ai.splitSourceDirs = realAppInfo.splitSourceDirs;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ai.splitNames= realAppInfo.splitNames;
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(Build.VERSION.SDK_INT < 26) {
                ApplicationInfoN.deviceEncryptedDataDir.set(ai, ai.dataDir);
                ApplicationInfoN.credentialEncryptedDataDir.set(ai, ai.dataDir);
            }
            ApplicationInfoN.deviceProtectedDataDir.set(ai, ai.dataDir);
            ApplicationInfoN.credentialProtectedDataDir.set(ai, ai.dataDir);
        }
//        try {
//            String key = ai.packageName + userId;
//            String label = mLabelCache.get(key);
//            if (label == null) {
//                CustomizeAppData data = CustomizeAppData.loadFromPref(ai.packageName, userId);
//                label = data.label;
//                mLabelCache.put(key, data.label);
//            }
//            ai.nonLocalizedLabel = label;
//        }catch (Throwable ex) {
//            ex.printStackTrace();
//        }
    }

    private static String choose32bitLibPath(ApplicationInfo applicationInfo) {
        if(Build.VERSION.SDK_INT >= 21) {
            try {
                String primaryCpuAbi = ApplicationInfoL.primaryCpuAbi.get(applicationInfo);
                String secondaryCpuAbi = ApplicationInfoL.secondaryCpuAbi.get(applicationInfo);
//                VLog.d("JJJJ","primary: " +primaryCpuAbi  + " second: " + secondaryCpuAbi
//                        + " nativelib: " + applicationInfo.nativeLibraryDir + " secondNativeLib: " + ApplicationInfoL.secondaryNativeLibraryDir.get(applicationInfo) ) ;
                if (primaryCpuAbi == null) {
//                    VLog.d("JJJJ","primaryCpuAbi == null");
                    return null;
                }

                boolean is64bitAbi = (boolean) VMRuntime.is64BitAbi.call(primaryCpuAbi);
                if (!is64bitAbi) {
//                    VLog.d("JJJJ","applicationInfo.nativeLibraryDir " + applicationInfo.nativeLibraryDir);
                    return applicationInfo.nativeLibraryDir;
                }

                if (secondaryCpuAbi != null
                        && !(boolean) VMRuntime.is64BitAbi.call(secondaryCpuAbi)) {
//                    VLog.d("JJJJ","secondary libdir ");
//                    VLog.d("JJJJ","secondary libdir " +(String) ApplicationInfoL.secondaryNativeLibraryDir.get(applicationInfo) );

                    return (String) ApplicationInfoL.secondaryNativeLibraryDir.get(applicationInfo) ;
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
//            VLog.d("JJJJ","last xreturn null" );
            return null;
        } else {
            return applicationInfo.nativeLibraryDir;
        }
    }

    private static void addOwner(VPackage p) {
        for (VPackage.ActivityComponent activity : p.activities) {
            activity.owner = p;
            for (VPackage.ActivityIntentInfo info : activity.intents) {
                info.activity = activity;
            }
        }
        for (VPackage.ServiceComponent service : p.services) {
            service.owner = p;
            for (VPackage.ServiceIntentInfo info : service.intents) {
                info.service = service;
            }
        }
        for (VPackage.ActivityComponent receiver : p.receivers) {
            receiver.owner = p;
            for (VPackage.ActivityIntentInfo info : receiver.intents) {
                info.activity = receiver;
            }
        }
        for (VPackage.ProviderComponent provider : p.providers) {
            provider.owner = p;
            for (VPackage.ProviderIntentInfo info : provider.intents) {
                info.provider = provider;
            }
        }
        for (VPackage.InstrumentationComponent instrumentation : p.instrumentation) {
            instrumentation.owner = p;
        }
        for (VPackage.PermissionComponent permission : p.permissions) {
            permission.owner = p;
        }
        for (VPackage.PermissionGroupComponent group : p.permissionGroups) {
            group.owner = p;
        }

        int flags = ApplicationInfo.FLAG_HAS_CODE;
        if(GmsSupport.isGoogleService(p.packageName)) {
            flags = ApplicationInfo.FLAG_HAS_CODE|ApplicationInfo.FLAG_PERSISTENT;
        }

        p.applicationInfo.flags |= flags;
    }

    public static PackageInfo generatePackageInfo(VPackage p, int flags, long firstInstallTime, long lastUpdateTime, PackageUserState state, int userId) {
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        if (p.mSignatures == null) {
            readSignature(p);
        }
//        if (p.mSignatures != null) {
//            for (Signature a:p.mSignatures) {
//                VLog.d(TAG, p.packageName + ": " + a.toCharsString());
//                try {
//                    MessageDigest md = MessageDigest.getInstance("SHA");
//                    md.update(a.toByteArray());
//                    VLog.d(TAG, "KeyHash:"+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
//                }catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//            }
//        }
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.versionCode = p.mVersionCode;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if (p.requestedPermissions != null && !p.requestedPermissions.isEmpty()) {
            String[] requestedPermissions = new String[p.requestedPermissions.size()];
            p.requestedPermissions.toArray(requestedPermissions);
            pi.requestedPermissions = requestedPermissions;
        }
        if ((flags & PackageManager.GET_GIDS) != 0) {
            pi.gids = PackageParserCompat.GIDS;
        }
        if ((flags & PackageManager.GET_CONFIGURATIONS) != 0) {
            int N = p.configPreferences != null ? p.configPreferences.size() : 0;
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            N = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
        }
        if ((flags & PackageManager.GET_ACTIVITIES) != 0) {
            final int N = p.activities.size();
            if (N > 0) {
                int num = 0;
                final ActivityInfo[] res = new ActivityInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ActivityComponent a = p.activities.get(i);
                    res[num++] = generateActivityInfo(a, flags, state, userId);
                }
                pi.activities = res;
            }
        }
        if ((flags & PackageManager.GET_RECEIVERS) != 0) {
            final int N = p.receivers.size();
            if (N > 0) {
                int num = 0;
                final ActivityInfo[] res = new ActivityInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ActivityComponent a = p.receivers.get(i);
                    res[num++] = generateActivityInfo(a, flags, state, userId);
                }
                pi.receivers = res;
            }
        }
        if ((flags & PackageManager.GET_SERVICES) != 0) {
            final int N = p.services.size();
            if (N > 0) {
                int num = 0;
                final ServiceInfo[] res = new ServiceInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ServiceComponent s = p.services.get(i);
                    res[num++] = generateServiceInfo(s, flags, state, userId);
                }
                pi.services = res;
            }
        }
        if ((flags & PackageManager.GET_PROVIDERS) != 0) {
            final int N = p.providers.size();
            if (N > 0) {
                int num = 0;
                final ProviderInfo[] res = new ProviderInfo[N];
                for (int i = 0; i < N; i++) {
                    final VPackage.ProviderComponent pr = p.providers.get(i);
                    res[num++] = generateProviderInfo(pr, flags, state, userId);
                }
                pi.providers = res;
            }
        }
        if ((flags & PackageManager.GET_INSTRUMENTATION) != 0) {
            int N = p.instrumentation.size();
            if (N > 0) {
                pi.instrumentation = new InstrumentationInfo[N];
                for (int i = 0; i < N; i++) {
                    pi.instrumentation[i] = generateInstrumentationInfo(
                            p.instrumentation.get(i), flags);
                }
            }
        }
        if ((flags & PackageManager.GET_SIGNATURES) != 0) {
            int N = (p.mSignatures != null) ? p.mSignatures.length : 0;
            if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N);
            }
        }
        return pi;
    }

    public static ApplicationInfo generateApplicationInfo(VPackage p, int flags,
                                                          PackageUserState state, int userId) {
        if (p == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }

        // Make shallow copy so we can store the metadata/libraries safely
        ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
        if ((flags & PackageManager.GET_META_DATA) != 0) {
            ai.metaData = p.mAppMetaData;
        }
        try {
        initApplicationAsUser(ai, userId);
        } catch (Exception e) {
            //avoid android O crash
        }
        return ai;
    }


    public static ActivityInfo generateActivityInfo(VPackage.ActivityComponent a, int flags,
                                                    PackageUserState state, int userId) {
        if (a == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        // Make shallow copies so we can store the metadata safely
        ActivityInfo ai = new ActivityInfo(a.info);
        if ((flags & PackageManager.GET_META_DATA) != 0
                && (a.metaData != null)) {
            ai.metaData = a.metaData;
        }
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static ServiceInfo generateServiceInfo(VPackage.ServiceComponent s, int flags,
                                                  PackageUserState state, int userId) {
        if (s == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        // Make shallow copies so we can store the metadata safely
        if ((flags & PackageManager.GET_META_DATA) != 0 && s.metaData != null) {
            si.metaData = s.metaData;
        }
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static ProviderInfo generateProviderInfo(VPackage.ProviderComponent p, int flags,
                                                    PackageUserState state, int userId) {
        if (p == null) return null;
        if (!checkUseInstalledOrHidden(state, flags)) {
            return null;
        }
        // Make shallow copies so we can store the metadata safely
        ProviderInfo pi = new ProviderInfo(p.info);
        if ((flags & PackageManager.GET_META_DATA) != 0
                && (p.metaData != null)) {
            pi.metaData = p.metaData;
        }

        if ((flags & PackageManager.GET_URI_PERMISSION_PATTERNS) == 0) {
            pi.uriPermissionPatterns = null;
        }
        pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
        return pi;
    }

    public static InstrumentationInfo generateInstrumentationInfo(
            VPackage.InstrumentationComponent i, int flags) {
        if (i == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static PermissionInfo generatePermissionInfo(
            VPackage.PermissionComponent p, int flags) {
        if (p == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    public static PermissionGroupInfo generatePermissionGroupInfo(
            VPackage.PermissionGroupComponent pg, int flags) {
        if (pg == null) return null;
        if ((flags & PackageManager.GET_META_DATA) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    private static boolean checkUseInstalledOrHidden(PackageUserState state, int flags) {
        //noinspection deprecation
        return (state.installed && !state.hidden)
                || (flags & PackageManager.GET_UNINSTALLED_PACKAGES) != 0;
    }

}
