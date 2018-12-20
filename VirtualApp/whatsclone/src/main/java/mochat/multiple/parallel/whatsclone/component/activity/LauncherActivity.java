package mochat.multiple.parallel.whatsclone.component.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.component.fragment.HomeFragment;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.utils.CloneHelper;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;

/**
 * Created by yxx on 2016/8/23.
 */
public class LauncherActivity extends BaseActivity{

    private static boolean created;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time = System.currentTimeMillis();
        setContentView(R.layout.activity_spc_splash);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        FuseAdLoader adLoader = FuseAdLoader.get(HomeFragment.SLOT_HOME_HEADER_NATIVE, this.getApplicationContext());
        adLoader.setBannerAdSize(HomeFragment.getBannerSize());
        adLoader.loadAd(1, null);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(LauncherActivity.this).preLoadClonedApp(LauncherActivity.this);
            }
        },100);

        //VirtualCore.get().waitForEngine();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceManagerNative.ensureServerStarted();
//                String srv = RemoteConfig.getString("fingerprint_svr");
//                if (!TextUtils.isEmpty(srv) && PreferencesUtils.hasCloned()
//                        && CommonUtils.isNetworkAvailable(LauncherActivity.this)) {
//                    Fingerprint.genFingerprint(LauncherActivity.this, srv);
//                }
            }
        }).start();
        long delta = System.currentTimeMillis() - time;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHome();
            }
        }, 2000 - delta);

    }


    public void createShortCut(){
        //创建快捷方式的Intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        //需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext() , LauncherActivity.class));
        //发送广播。OK
        sendBroadcast(shortcutintent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    private void enterHome(){
        if(!PreferencesUtils.isShortCutCreated() && !created) {
            PreferencesUtils.setShortCutCreated();
            createShortCut();
            created = true;
        }
        HomeActivity.enter(this, needUpdate());
        finish();
    }

    private boolean needUpdate() {
        try {
            PackageInfo vinfo = getPackageManager().getPackageInfo(getPackageName(),0);
            int versionCode = vinfo.versionCode;
            long pushVersion = RemoteConfig.getLong(AppConstants.CONF_UPDATE_VERSION);
            long latestVersion = RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION);
            long ignoreVersion = PreferencesUtils.getIgnoreVersion();
            MLogs.d("local: " + versionCode + " push: " + pushVersion + " latest: " + latestVersion + " ignore: "+ ignoreVersion);
            if (versionCode <= pushVersion
                   && ignoreVersion < latestVersion) {
                return true;
            }
        }catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }
}
