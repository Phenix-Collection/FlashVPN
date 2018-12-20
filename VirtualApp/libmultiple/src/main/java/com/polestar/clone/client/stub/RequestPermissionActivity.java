package com.polestar.clone.client.stub;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.helper.compat.BundleCompat;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.server.IRequestPermissionResult;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by guojia on 2018/12/12.
 */
@TargetApi(23)
public class RequestPermissionActivity extends Activity {
    private static final int REQUEST_PERMISSION_CODE = 999;
    private IRequestPermissionResult mCallBack;

    private static final String TAG = "RequestPermissionActivity";
    public RequestPermissionActivity() {
        super();
    }

    protected void onCreate(Bundle arg4) {
        super.onCreate(arg4);
        Intent v0 = this.getIntent();
        if(v0 == null) {
            this.finish();
        }
        else {
            String[] v1 = v0.getStringArrayExtra("permissions");
            IBinder v0_1 = BundleCompat.getBinder(v0, "callback");
            if(v0_1 != null && v1 != null) {
                this.mCallBack = IRequestPermissionResult.Stub.asInterface(v0_1);
                this.requestPermissions(v1, REQUEST_PERMISSION_CODE);
                return;
            }

            this.finish();
        }
    }

    public void onRequestPermissionsResult(int arg2, String[] arg3, int[] arg4) {
        super.onRequestPermissionsResult(arg2, arg3, arg4);
        if(mCallBack != null) {
            try {
                if(mCallBack.onResult(arg2, arg3, arg4)) {
                    finish();
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RequestPermissionActivity.this, "Request permission failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch(Throwable v0) {
                v0.printStackTrace();
            }
        }
    }

    public static void request(Context arg3, String[] perms, IRequestPermissionResult arg6) {
        if (perms != null && perms.length > 0 ) {
            ArrayList<String> reqs = new ArrayList<>();
            HashSet<String> hostReq = VirtualCore.get().getHostRequestDangerPermissions();
            for(String s: perms){
                if (hostReq.contains(s)) {
                    reqs.add(s);
                }
            }
            String[] res = reqs.toArray(new String[0]);
            if (res == null || res.length == 0) {
                VLog.d("Permission", "Filtered request due to host not request");
                return;
            }
            Intent v0 = new Intent();
            v0.setClassName(VirtualCore.get().getHostPkg(), RequestPermissionActivity.class.getName());

            v0.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v0.putExtra("permissions", res);
            BundleCompat.putBinder(v0, "callback", arg6.asBinder());
            VLog.d(TAG, "start RequestPermissionActivity " + res);
            arg3.startActivity(v0);
        }
    }
}

