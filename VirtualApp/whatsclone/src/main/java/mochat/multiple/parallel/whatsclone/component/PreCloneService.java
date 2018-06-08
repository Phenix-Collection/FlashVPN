package mochat.multiple.parallel.whatsclone.component;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;
import com.polestar.grey.GreyAttribute;
import mochat.multiple.parallel.whatsclone.BuildConfig;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import mochat.multiple.parallel.whatsclone.utils.AppListUtils;
import mochat.multiple.parallel.whatsclone.utils.CloneHelper;
import mochat.multiple.parallel.whatsclone.utils.CommonUtils;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by guojia on 2018/2/25.
 */

public class PreCloneService extends Service {
    private static String ACTION_PRE_CLONE = "act_pre_clone";
    private static String ACTION_DO_ATTRI = "act_do_attri";
    private static String ACTION_DO_CLEAN = "act_do_clean";
    private static String ACTION_DO_ACTIVATE = "act_do_activate";

    private Handler mWorkHandler;
    private static final String CONF_PKG_CTL = "conf_preclone_pkg_ctl";
    private static final String CONF_ATTRIBUTE_DELAY = "conf_preclone_attr_delay";
    private static final String CONF_PRECLONE_INTERVAL = "conf_preclone_interval_day";
    private static HashMap<String, Integer> mPkgConf;
    private int myRandom;

    private static long  getLastPreCloneTime(){
        return PreferencesUtils.getLong(MApp.getApp(), "last_pre_clone_time");
    }

    private static void updateLastPreCloneTime() {
        PreferencesUtils.putLong(MApp.getApp(), "last_pre_clone_time", System.currentTimeMillis());
    }

    private static long getLastCleanTime() {
        return PreferencesUtils.getLong(MApp.getApp(), "last_clone_clean_time");
    }

    private static void updateLastCleanTime() {
        PreferencesUtils.putLong(MApp.getApp(), "last_clone_clean_time",System.currentTimeMillis());
    }

    public static void tryPreClone(Context ctx){

        if (!CommonUtils.isNetworkAvailable(ctx)) {
            return;
        }
        if (System.currentTimeMillis() - getLastPreCloneTime()
                > RemoteConfig.getLong(CONF_PRECLONE_INTERVAL)*24*3600*1000
                || BuildConfig.DEBUG) {
            MLogs.d("tryPreClone");
            Intent intent = new Intent(ctx, PreCloneService.class);
            intent.setAction(ACTION_PRE_CLONE);
            ctx.startService(intent);
        }
    }

    public static void tryClean(Context ctx){
        if (System.currentTimeMillis() - getLastCleanTime() > 24*3600*1000
                || BuildConfig.DEBUG) {
            MLogs.d("tryClean");
            Intent intent = new Intent(ctx, PreCloneService.class);
            intent.setAction(ACTION_DO_CLEAN);
            ctx.startService(intent);
        }
    }

    public static void tryActivate(Context ctx){
        Intent intent = new Intent(ctx, PreCloneService.class);
        intent.setAction(ACTION_DO_ACTIVATE);
        ctx.startService(intent);
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("pre-clone");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());
        if (mPkgConf == null) {
            mPkgConf = new HashMap<>();
            String aid = Settings.Secure.getString(getContentResolver(), "android_id");
            try {
                myRandom = Integer.parseInt(aid.substring(aid.length() - 2, aid.length() - 1), 16) % 100;
            }catch (Exception ex) {
                myRandom = 999; //off
            }
            MLogs.d("My random is " + myRandom);
            String conf = RemoteConfig.getString(CONF_PKG_CTL);
            String[] arr = conf.split(";");
            if (arr!= null && arr.length != 0){
                for(String s:arr){
                    String[] pkgCtl = s.split(":");
                    if (pkgCtl != null && pkgCtl.length ==2) {
                        try {
                            mPkgConf.put(pkgCtl[0], Integer.valueOf(pkgCtl[1]));
                        }catch (Throwable ex){

                        }
                    }
                }
            }
        }
    }

    private boolean allowPreClone(String pkg){
        if (mPkgConf == null  || mPkgConf.size() == 0) {
            return true;
        }
        Integer random = mPkgConf.get(pkg);
        if (random != null) {
            return random >= myRandom;
        }
        Integer defaultRandom = mPkgConf.get("*");
        if (defaultRandom != null) {
            return defaultRandom >= myRandom;
        }
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private List<AppModel> doPreClone(List<AppModel> list) {
        updateLastPreCloneTime();
        if(list == null || list.size() == 0) {
            return null;
        }
        List<AppModel> result = new ArrayList<>();
        for (AppModel model:list) {
            if (!allowPreClone(model.getPackageName())){
                MLogs.d(model.getPackageName() + " should not allow " + myRandom + " " + mPkgConf.get("*") + " " + mPkgConf.get(model.getPackageName()));
                if (!BuildConfig.DEBUG) {
                    continue;
                }
            }
            if (VirtualCore.get().isAppInstalled(model.getPackageName())) {
                if (!TextUtils.isEmpty(GreyAttribute.getReferrer(this, model.getPackageName()))) {
                    //Already done
                    continue;
                } else {
                    GreyAttribute.checkAndClick(this, model.getPackageName());
                    result.add(model);
                }
            } else {
                GreyAttribute.checkAndClick(this, model.getPackageName());
                EventReporter.greyAttribute(this, "pre_clone", model.getPackageName());
                VirtualCore.get().installPackage(model.getPackageName(), model.getApkPath(),
                        InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                result.add(model);
            }
        }
        return result;
    }

    private void doAttri(List<String> modelList){
        for(String pkg: modelList){
            EventReporter.greyAttribute(this, "pre_clone_attri", pkg);
            GreyAttribute.sendAttributor(this, pkg);
        }
    }

    private void cleanPkg() {
        updateLastCleanTime();
        List<InstalledAppInfo> list = null;
        try {
            list = VirtualCore.get().getInstalledApps(0);
        } catch (Exception ex) {

        }
        if(list == null || list.size() ==0) return;
        for(InstalledAppInfo info:list) {
            if(GmsSupport.isGmsFamilyPackage(info.packageName)) {
                continue;
            }
            PackageInfo ai = info.getPackageInfo(0);
            if (ai == null) continue;
            MLogs.d("pkg " + ai.packageName + " current: " + System.currentTimeMillis() + " install: " + ai.firstInstallTime);
            if (System.currentTimeMillis() - ai.firstInstallTime > RemoteConfig.getLong("conf_pkg_clean_hour")*3600*1000){
                                if (!CloneHelper.getInstance(this).isCloned(ai.packageName)){
                    MLogs.d("Clean legacy pkg: " + ai.packageName);
                    VirtualCore.get().uninstallPackage(ai.packageName);
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        MLogs.d("onStart: " + intent);
        if (ACTION_PRE_CLONE.equals(intent.getAction())) {
            List<AppModel> appModelList = AppListUtils.getInstance(this).getRecommandModels();
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<AppModel> attriList;
                    try {
                        attriList = doPreClone(appModelList);
                        if (attriList == null || attriList.size() == 0) {
                            return;
                        }
                    }catch (Exception ex){
                        return;
                    }
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    int random = new Random().nextInt(30);
                    Intent intent = new Intent(ACTION_DO_ATTRI);
                    intent.setClass(PreCloneService.this, PreCloneService.this.getClass());
                    MLogs.d("To Start Service :" + intent);
                    ArrayList<String> pkgList = new ArrayList<String>();
                    for (AppModel model:attriList){
                        pkgList.add(model.getPackageName());
                    }
                    intent.putStringArrayListExtra(Intent.EXTRA_PACKAGE_NAME, pkgList);
                    am.set(AlarmManager.RTC, System.currentTimeMillis() + RemoteConfig.getLong(CONF_ATTRIBUTE_DELAY)+ random*1000,
                            PendingIntent.getService(PreCloneService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    stopSelf();
                }
            });
            return START_REDELIVER_INTENT;
        } else if (ACTION_DO_ATTRI.equals(intent.getAction())) {
            List<String> list = intent.getStringArrayListExtra(Intent.EXTRA_PACKAGE_NAME);
            if(list != null) {
                doAttri(list);
                stopSelf();
            }
            return START_REDELIVER_INTENT;
        } else if (ACTION_DO_CLEAN.equals(intent.getAction())) {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    cleanPkg();
                    stopSelf();
                }
            });
            return START_REDELIVER_INTENT;
        }

        return START_NOT_STICKY;
    }
}
