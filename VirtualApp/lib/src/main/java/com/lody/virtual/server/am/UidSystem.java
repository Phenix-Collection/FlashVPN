package com.lody.virtual.server.am;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.FileUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VEnvironment;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.os.Process.FIRST_APPLICATION_UID;

/**
 * @author Lody
 */

public class UidSystem {

    private static final String TAG = UidSystem.class.getSimpleName();

    private final HashMap<String, Integer> mSharedUserIdMap = new HashMap<>();
    private static final int FREE_UID_START = FIRST_APPLICATION_UID + 5000;
    private int mFreeUid = FREE_UID_START;


    public void initUidList() {
        mSharedUserIdMap.clear();
        File uidFile = VEnvironment.getUidListFile();
        if (!loadUidList(uidFile)) {
            File bakUidFile = VEnvironment.getBakUidListFile();
            loadUidList(bakUidFile);
        }
    }

    private boolean loadUidList(File uidFile) {
        if (!uidFile.exists()) {
            return false;
        }
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(uidFile));
            mFreeUid = is.readInt();
            if (mFreeUid < FREE_UID_START) {
                mFreeUid = FREE_UID_START;
            }
            //noinspection unchecked
            Map<String, Integer> map = (HashMap<String, Integer>) is.readObject();
            mSharedUserIdMap.putAll(map);
            is.close();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    private void save() {
        File uidFile = VEnvironment.getUidListFile();
        File bakUidFile = VEnvironment.getBakUidListFile();
        if (uidFile.exists()) {
            if (bakUidFile.exists() && !bakUidFile.delete()) {
                VLog.w(TAG, "Warning: Unable to delete the expired file --\n " + bakUidFile.getPath());
            }
            try {
                FileUtils.copyFile(uidFile, bakUidFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(uidFile));
            os.writeInt(mFreeUid);
            os.writeObject(mSharedUserIdMap);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getOrCreateUid(VPackage pkg) {
        String sharedUserId = pkg.mSharedUserId;
        if (sharedUserId == null) {
            sharedUserId = pkg.packageName;
        }
        Integer uid = mSharedUserIdMap.get(sharedUserId);
        if (uid != null) {
            return uid;
        }
        int newUid;
        try {
            PackageInfo pi = VirtualCore.get().getUnHookPackageManager().getPackageInfo(pkg.packageName, 0);
            newUid = pi.applicationInfo.uid;
        } catch (Exception ex) {
            newUid = ++mFreeUid;
            VLog.logbug(TAG, "Not found package infor for "+ pkg.packageName);
            VLog.logbug(TAG, VLog.getStackTraceString(ex));
        }
        mSharedUserIdMap.put(sharedUserId, newUid);
        save();
        return newUid;
    }
}
