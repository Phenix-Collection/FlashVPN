package com.polestar.multiaccount.component;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lody.virtual.Build;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.grey.GreyAttribute;
import com.polestar.multiaccount.BuildConfig;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by guojia on 2018/2/25.
 */

public class PreCloneService extends Service {
    private static String ACTION_PRE_CLONE = "act_pre_clone";

    private Handler mWorkHandler;
    private final String CONF_PKG_CTL = "conf_preclone_pkg_ctl";
    private final String CONF_ATTRIBUTE_DELAY = "conf_preclone_attr_delay";
    private static HashMap<String, Integer> mPkgConf;
    private int myRandom;

    public static void tryPreClone(Context ctx){
        Intent intent = new Intent(ctx, PreCloneService.class);
        intent.setAction(ACTION_PRE_CLONE);
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
            myRandom = Integer.parseInt(aid.substring(aid.length()-2, aid.length()-1), 16)%100;
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
                VirtualCore.get().installPackage(model.getPackageName(), model.getApkPath(),
                        InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                result.add(model);
            }
        }
        return result;
    }

    private void doAttri(List<AppModel> modelList){
        for(AppModel model: modelList){

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        if (ACTION_PRE_CLONE.equals(intent.getAction())) {
            List<AppModel> appModelList = AppListUtils.getInstance(this).getRecommandModels();
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<AppModel> attriList = doPreClone(appModelList);
                    try {
                        Thread.sleep(RemoteConfig.getLong(CONF_ATTRIBUTE_DELAY));
                    }catch (Throwable ex){

                    }
                    doAttri(attriList);
                    stopSelf();
                }
            });
            return START_REDELIVER_INTENT;
        }

        return START_NOT_STICKY;
    }
}
