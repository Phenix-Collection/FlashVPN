package mochat.multiple.parallel.whatsclone.utils;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;

import android.app.Activity;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.widgets.UpDownDialog;

/**
 * Created by guojia on 2018/12/21.
 */

public class PermissionManager {
    private Activity mActivity;
    private int mRequestCode;
    private static final String CONFIG_FORCE_REQUESTED_PERMISSIONS = "force_requested_permission";

    public PermissionManager(Activity activity, int requestCode) {
        mActivity = activity;
        mRequestCode = requestCode;
    }

    //return true if need to apply permission
    public boolean applyPermissionIfNeeded(){
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
                if (mActivity.checkCallingOrSelfPermission(s) != PackageManager.PERMISSION_GRANTED) {
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
                    if (mActivity.shouldShowRequestPermissionRationale(s)){
                        showRequestRational = true;
                    }
                }
                if (showRequestRational
                        || !PreferencesUtils.hasShownPermissionGuide()) {
                    showPermissionGuideDialog(toRequest);
                } else {
                    mActivity.requestPermissions(toRequest, mRequestCode);
                }
                return true;
            }
        }
        return  false;
    }

    @TargetApi(23)
    private void showPermissionGuideDialog(String[] perms) {
        EventReporter.generalEvent("show_permission_guide");
        PreferencesUtils.setShownPermissionGuide(true);
        UpDownDialog.show(mActivity, mActivity.getString(R.string.dialog_permission_title),
                mActivity.getString(R.string.dialog_permission_content), null, mActivity.getString(R.string.ok),
                R.drawable.dialog_tag_comment, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventReporter.generalEvent("ok_permission_guide");
                        mActivity.requestPermissions(perms, mRequestCode);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                EventReporter.generalEvent("cancel_permission_guide");
                mActivity.requestPermissions(perms, mRequestCode);
            }
        });
    }
}
