package com.polestar.clone;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;

import com.polestar.clone.client.core.InstallStrategy;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.client.ipc.VPackageManager;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserInfo;
import com.polestar.clone.os.VUserManager;
import com.polestar.clone.remote.InstallResult;
import com.polestar.clone.remote.InstalledAppInfo;

import java.io.File;

/**
 * Created by guojia on 2018/5/23.
 */

public class CloneAgentService extends Service {
    private static final String TAG = "CloneAgent";
    @Override
    public IBinder onBind(Intent intent) {
        VLog.d(TAG, "onBind " + intent);
        return new CloneAgent();
    }

    private class CloneAgent extends ICloneAgent.Stub {
        public void createClone(String pkg, int userId){
            try {
                InstalledAppInfo info = VirtualCore.get().getInstalledAppInfo(pkg, 0);
                if (info != null) {
                    if (VUserManager.get().getUserInfo(userId) == null) {
                        // user not exist, create it automatically.
                        String nextUserName = "User " + (userId + 1);
                        VUserInfo newUserInfo = VUserManager.get().createUser(nextUserName, VUserInfo.FLAG_ADMIN);
                        if (newUserInfo == null) {
                            throw new IllegalStateException();
                        }
                    }
                    VirtualCore.get().installPackageAsUser(userId, pkg);
                } else {
                    PackageManager pm = getPackageManager();
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    if (ai != null) {
                        InstallResult result = VirtualCore.get().installPackage(pkg, ai.sourceDir,
                                InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                    }
                }
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
        }
        public void deleteClone(String pkg, int userId){
            try {
                VirtualCore.get().uninstallPackageAsUser(pkg, userId);
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
        }
        public void launchApp(String pkg, int userId){
            try {
                Intent intent = VirtualCore.get().getLaunchIntent(pkg, userId);
                VActivityManager.get().startActivity(intent, userId);
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
        }
        public boolean isNeedUpgrade(String pkg){
            try {
                PackageInfo vinfo = VPackageManager.get().getPackageInfo(pkg, 0 ,0);
                PackageInfo info = VirtualCore.get().getUnHookPackageManager().getPackageInfo(pkg,0);
                if (vinfo == null || info == null) {
                    return false;
                }
                return vinfo.versionCode != info.versionCode;
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
            return false;
        }
        public void upgradeApp(String pkg){
            try {
                PackageInfo info = VirtualCore.get().getUnHookPackageManager().getPackageInfo(pkg, 0);
                InstallResult result = VirtualCore.get().upgradePackage(info.packageName, info.applicationInfo.sourceDir,
                        InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.UPDATE_IF_EXIST);
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
        }

        public boolean isCloned(String pkg, int userId){
            try {
                return VirtualCore.get().isAppInstalledAsUser(userId, pkg);
            }catch (Exception ex){
                VLog.logbug(TAG, ex.toString());
            }
            return false;
        }

        public void syncGlobalSetting(String lockKey, int lockInterval, boolean adFree, boolean quickSwitch){
            VirtualCore.get().getComponentDelegate().reloadSetting(lockKey, adFree, lockInterval , quickSwitch);
        }

        public void syncPackageSetting(String pkg, int userId, CustomizeAppData data) {
            data.saveToPref();
            if (data.customized) {
                try{
                    File dir = new File(getFilesDir() + BitmapUtils.ICON_FILE_PATH );
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String pathname = BitmapUtils.getCustomIconPath(CloneAgentService.this, pkg, userId);
                    Drawable defaultIcon = getPackageManager().getApplicationIcon(pkg);
                    Bitmap customIcon = BitmapUtils.handleImageEffect(BitmapUtils.drawableToBitmap(defaultIcon), data.hue, data.sat,data.light );
                    if (data.badge) {
                        customIcon = BitmapUtils.createBadgeIcon(CloneAgentService.this, new BitmapDrawable(customIcon), data.userId);
                    }
                    BitmapUtils.saveBitmapToPNG(customIcon, pathname);
                } catch (Exception e) {
                    VLog.logbug(TAG, e.toString());
                }
            }
        }

        public boolean isAppRunning(String pkg, int userId) {
            try {
                return VirtualCore.get().isAppRunning(pkg, userId);
            }catch (Throwable ex){
                return false;
            }
        }
    }
}
