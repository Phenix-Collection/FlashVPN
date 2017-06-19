package com.lody.virtual.server.pm;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Parcel;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.secondary.GmsSupport;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.PersistenceLayer;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.server.pm.parser.VPackage;

import java.util.Arrays;

/**
 * @author Lody
 */

class PackagePersistenceLayer extends PersistenceLayer {

    private static final char[] MAGIC = {'v', 'p', 'k', 'g'};
    private static final int CURRENT_VERSION = 3;
    private static final String TAG = "PersistenceLayer";

    private VAppManagerService mService;

    PackagePersistenceLayer(VAppManagerService service) {
        super(VEnvironment.getPackageListFile());
        mService = service;
    }

    @Override
    public int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public void writeMagic(Parcel p) {
        p.writeCharArray(MAGIC);
    }

    @Override
    public boolean verifyMagic(Parcel p) {
        char[] magic = p.createCharArray();
        return Arrays.equals(magic, MAGIC);
    }


    @Override
    public void writePersistenceData(Parcel p) {
        synchronized (PackageCacheManager.PACKAGE_CACHE) {
            p.writeInt(PackageCacheManager.PACKAGE_CACHE.size());
            for (VPackage pkg : PackageCacheManager.PACKAGE_CACHE.values()) {
                PackageSetting ps = (PackageSetting) pkg.mExtras;
                ps.writeToParcel(p, 0);
            }
        }
    }

    @Override
    public void readPersistenceData(Parcel p) {
        int count = p.readInt();
        VLog.d(TAG, "GMS state: " + StubManifest.ENABLE_GMS);
        while (count-- > 0) {
            PackageSetting setting = new PackageSetting(p);
            if (!StubManifest.ENABLE_GMS && GmsSupport.isGmsFamilyPackage(setting.packageName)) {
                VLog.d(TAG, "Skip loading gms package: " + setting.packageName);
                continue;
            }
            if (!"android".equals(setting.packageName)) {
                if (GmsSupport.hasDexFile(setting.apkPath)) {
                    mService.loadPackage(setting);
                }
            }
            VLog.d(TAG, "read package: " + setting.packageName);
        }
        if (!VirtualCore.get().isAppInstalled(GmsSupport.GSF_PKG)) {
            GmsSupport.removeGmsPackage(GmsSupport.GSF_PKG);
        }
    }

    @Override
    public boolean onVersionConflict(int fileVersion, int currentVersion) {
        VLog.logbug(TAG, "Version conflict: " + fileVersion + " current: " + currentVersion);
        return true;
    }

    @Override
    public void onPersistenceFileDamage() {
        getPersistenceFile().delete();
        VAppManagerService.get().restoreFactoryState();
    }

    private void saveOSVersion() {
        SharedPreferences sp = VirtualCore.get().getContext().getSharedPreferences("spc_config", Context.MODE_PRIVATE);
        sp.edit().putString("osv", Build.FINGERPRINT).apply();
    }

    private String getOSVersion() {
        SharedPreferences sp = VirtualCore.get().getContext().getSharedPreferences("spc_config", Context.MODE_PRIVATE);
        return sp.getString("osv", null);
    }

    @Override
    public boolean verifyOSUpgrade() {
        String osv = getOSVersion();
        boolean ret;
        if (osv == null) {
            ret = true;
        } else {
            if (!osv.equals(Build.FINGERPRINT)) {
                VLog.keyLog(VirtualCore.get().getContext(), this.getClass().getSimpleName(), "OS upgrade! new :" + Build.FINGERPRINT + " old: " + osv);
                saveOSVersion();
                VEnvironment.getDalvikCacheDirectory().delete();
                ret = false;
            } else {
                ret = true;
            }
        }
        return  ret;
    }
}
