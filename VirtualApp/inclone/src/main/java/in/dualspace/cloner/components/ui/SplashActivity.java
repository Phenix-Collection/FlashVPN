package in.dualspace.cloner.components.ui;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;
import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.utils.CommonUtils;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.widget.UpDownDialog;

import java.util.ArrayList;

/**
 * Created by DualApp on 2017/7/15.
 */

public class SplashActivity extends BaseActivity {

    private static boolean created;
    public final static String EXTRA_FROM_SHORTCUT = "extra_from_shortcut";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLogs.d(this.getClass().getName() +" launching from intent: " +getIntent());
        long time = System.currentTimeMillis();
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        EventReporter.reportWake(this, "user_launch");

        if (!PreferencesUtils.isAdFree() && !DualApp.isArm64()) {
            FuseAdLoader adLoader = FuseAdLoader.get(HomeActivity.SLOT_HOME_NATIVE, this.getApplicationContext());
            adLoader.setBannerAdSize(HomeActivity.getBannerAdSize());
            adLoader.preloadAd(this);
        }
        Handler handler = new Handler();
        CloneManager.getInstance(this).loadClonedApps(this, null);

        //VirtualCore.get().waitForEngine();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceManagerNative.ensureServerStarted();
            }
        }).start();
        long delta = System.currentTimeMillis() - time;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHome();
            }
        }, 2500 - delta);

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
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

    private void installSuccess() {
        Toast.makeText(this, "Install Success", Toast.LENGTH_SHORT).show();
        getPackageManager().setComponentEnabledSetting(new ComponentName(this, SplashActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }

    private void enterHome(){
        if(DualApp.isArm64()) {
            if(!applyPermissionIfNeeded()) {
               installSuccess();
               finish();
            }
        } else {
            if (!PreferencesUtils.isShortCutCreated()) {
                PreferencesUtils.setShortCutCreated();
                CommonUtils.createLaunchShortcut(this);
                created = true;
            }
//            HomeActivity.enter(this, needUpdate());
            finish();
        }
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
