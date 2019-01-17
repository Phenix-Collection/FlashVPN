package mochat.multiple.parallel.whatsclone.component.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;

import java.util.ArrayList;

import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.component.fragment.HomeFragment;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.utils.AppListUtils;
import mochat.multiple.parallel.whatsclone.utils.CloneHelper;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;
import mochat.multiple.parallel.whatsclone.widgets.UpDownDialog;

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
        if (MApp.isArm64()) {
            TextView text = (TextView)findViewById(R.id.txt_tips);
            text.setVisibility(View.VISIBLE);
        }
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        FuseAdLoader adLoader = FuseAdLoader.get(HomeFragment.SLOT_HOME_HEADER_NATIVE, this);
        adLoader.setBannerAdSize(HomeFragment.getBannerSize());
        adLoader.preloadAd(this);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(LauncherActivity.this).preLoadClonedApp(LauncherActivity.this);
            }
        },100);
        if(!PreferencesUtils.hasCloned()) {
            AppListUtils.getInstance(this);
        }

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
        if(MApp.isArm64()) {
            if(!applyPermissionIfNeeded()) {
                installSuccess();
                finish();
            }
        } else {
            if (!PreferencesUtils.isShortCutCreated()) {
                PreferencesUtils.setShortCutCreated();
                createShortCut();
                created = true;
            }
            HomeActivity.enter(this, needUpdate());
            finish();
        }
    }

    private void installSuccess() {
        Toast.makeText(this, "Installed 64bit library. Please launch clones from WhatsClone. ", Toast.LENGTH_SHORT).show();
        getPackageManager().setComponentEnabledSetting(new ComponentName(this, LauncherActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

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

    private static final String CONFIG_FORCE_REQUESTED_PERMISSIONS = "force_requested_permission";
    private static final int REQUEST_APPLY_PERMISSION = 101;

    @TargetApi(23)
    private void showPermissionGuideDialog(String[] perms) {
        EventReporter.generalEvent("show_permission_guide");
        PreferencesUtils.setShownPermissionGuide(true);
        UpDownDialog.show(this, getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_content), null, getString(R.string.ok),
                R.drawable.dialog_tag_comment, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventReporter.generalEvent("ok_permission_guide");
                        requestPermissions(perms, REQUEST_APPLY_PERMISSION);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                EventReporter.generalEvent("cancel_permission_guide");
                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
            }
        });
    }

    private boolean applyPermissionIfNeeded(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String conf = RemoteConfig.getString(CONFIG_FORCE_REQUESTED_PERMISSIONS);
            if (TextUtils.isEmpty(conf)) {
                return false;
            }
            String[] perms = conf.split(";");
            if (perms == null || perms.length == 0) {
                return false;
            }
            ArrayList<String> requestPerms = new ArrayList<>();
            for (String s : perms) {
                if (checkCallingOrSelfPermission(s) != PackageManager.PERMISSION_GRANTED) {
                    requestPerms.add(s);
                }
            }
            if (requestPerms.size() == 0) {
                EventReporter.setUserProperty(EventReporter.PROP_PERMISSION, "granted");
                return false;
            } else {
                EventReporter.setUserProperty(EventReporter.PROP_PERMISSION, "not_granted");
                String[] toRequest = requestPerms.toArray(new String[0]);
                boolean showRequestRational = false;
                for (String s: toRequest) {
                    if (shouldShowRequestPermissionRationale(s)){
                        showRequestRational = true;
                    }
                }
                if (showRequestRational
                        || !PreferencesUtils.hasShownPermissionGuide()) {
                    showPermissionGuideDialog(toRequest);
                } else {
                    requestPermissions(toRequest, REQUEST_APPLY_PERMISSION);
                }
                return true;
            }
        }
        return  false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        /* callback - no nothing */
        switch (requestCode){
            case REQUEST_APPLY_PERMISSION:
                int i = 0;
                boolean success = true;
                for(String p: permissions){
                    if(grantResults[i++] != PackageManager.PERMISSION_GRANTED) {
                        success = false;
                        EventReporter.generalEvent("fail_"+p);
                    }
                }
                EventReporter.generalEvent("apply_permission_" + success);
                MLogs.d("Apply permission result: " + success);
                if (success) {
                    installSuccess();
                }
                finish();
                break;
        }
    }
}
