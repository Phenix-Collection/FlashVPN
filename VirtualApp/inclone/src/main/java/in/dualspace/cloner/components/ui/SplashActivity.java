package in.dualspace.cloner.components.ui;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.polestar.clone.client.ipc.ServiceManagerNative;

import java.util.ArrayList;

import in.dualspace.cloner.R;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.widget.UpDownDialog;

/**
 * Created by guojia on 2019/3/13.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        //VirtualCore.get().waitForEngine();

        enterHome();
    }

    private static final String CONFIG_FORCE_REQUESTED_PERMISSIONS = "force_requested_permission";
    private static final int REQUEST_APPLY_PERMISSION = 101;

    @TargetApi(23)
    private void showPermissionGuideDialog(String[] perms) {
        EventReporter.generalEvent("show_permission_guide");
        UpDownDialog.show(this, getString(R.string.dialog_permission_title),
                getString(R.string.dialog_permission_content), getString(R.string.disagree), getString(R.string.agree),
                -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case UpDownDialog.POSITIVE_BUTTON:
                                EventReporter.generalEvent("ok_permission_guide");
                                PreferencesUtils.setShownPermissionGuide(true);
                                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
                                break;
                            case UpDownDialog.NEGATIVE_BUTTON:
                                EventReporter.generalEvent("disagree_permission_guide");
                                finish();
                                break;

                        }

                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                EventReporter.generalEvent("cancel_permission_guide");
//                requestPermissions(perms, REQUEST_APPLY_PERMISSION);
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
        if(!applyPermissionIfNeeded()) {
            installSuccess();
            finish();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ServiceManagerNative.ensureServerStarted();
                }
            }).start();
        }
    }
}