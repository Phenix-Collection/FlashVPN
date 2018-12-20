package com.polestar.clone.helper.compat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.stub.RequestPermissionActivity;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.server.IRequestPermissionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionCompat {
    public interface CallBack {
        boolean onResult(int arg1, String[] arg2, int[] arg3);
    }

    public static Set DANGEROUS_PERMISSION;

    static {
        PermissionCompat.DANGEROUS_PERMISSION = new HashSet<String>();
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_CALENDAR");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.WRITE_CALENDAR");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.CAMERA");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_CONTACTS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.WRITE_CONTACTS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.GET_ACCOUNTS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.ACCESS_FINE_LOCATION");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.ACCESS_COARSE_LOCATION");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_PHONE_STATE");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.CALL_PHONE");
        if(Build.VERSION.SDK_INT >= 16) {
            PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_CALL_LOG");
            PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.WRITE_CALL_LOG");
        }

        PermissionCompat.DANGEROUS_PERMISSION.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.USE_SIP");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.PROCESS_OUTGOING_CALLS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.SEND_SMS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.RECEIVE_SMS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_SMS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.RECEIVE_WAP_PUSH");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.RECEIVE_MMS");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.RECORD_AUDIO");
        PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.WRITE_EXTERNAL_STORAGE");
        if(Build.VERSION.SDK_INT >= 16) {
            PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.READ_EXTERNAL_STORAGE");
        }

        if(Build.VERSION.SDK_INT >= 20) {
            PermissionCompat.DANGEROUS_PERMISSION.add("android.permission.BODY_SENSORS");
        }
    }

    public PermissionCompat() {
    }

    public static boolean checkPermissions(String[] perms) {
        if(perms != null && perms.length > 0) {
            for (String perm: perms) {
                if (!VirtualCore.get().checkSelfPermission(perm)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String[] findDangerousPermissions(List<String> arg4) {
        String[] res;
        if(arg4 == null || arg4.size() == 0) {
            res = null;
        }
        else {
            ArrayList v1 = new ArrayList();
            for (String perm : arg4) {
                if (PermissionCompat.DANGEROUS_PERMISSION.contains(perm)) {
                    VLog.d("Permission", "danger: " + perm);
                    v1.add(perm);
                }
            }
            res = (String[])v1.toArray(new String[0]);
        }
        return res;
    }

    public static String[] findDangrousPermissions(String[] arg6) {
        Object[] v0_2 = null ;
        if(arg6 == null || arg6.length == 0) {
            String[] v0 = null;
        }
        else {
            ArrayList v2 = new ArrayList();
            int v3 = arg6.length;
            int v0_1;
            for(v0_1 = 0; v0_1 < v3; ++v0_1) {
                String v4 = arg6[v0_1];
                if(PermissionCompat.DANGEROUS_PERMISSION.contains(v4)) {
                    ((List)v2).add(v4);
                }
            }

            v0_2 = ((List)v2).toArray(new String[0]);
        }

        return ((String[])v0_2);
    }

    public static boolean isCheckPermissionRequired(int apiLevel) {

        return  Build.VERSION.SDK_INT >= 23 && apiLevel < 23;
    }

    public static boolean isRequestGranted(int[] arg6) {
        if (arg6 == null || arg6.length == 0) {
            return false;
        }

        for (int res: arg6) {
            if (res != PackageManager.PERMISSION_DENIED) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static void startRequestPermissions(Context arg1, String[] arg3, CallBack arg4) {
        RequestPermissionActivity.request(arg1, arg3, new PermissionRequestCallback(arg4));
    }

    static class PermissionRequestCallback extends IRequestPermissionResult.Stub{
        private CallBack mCb;
        PermissionRequestCallback(CallBack arg1) {
            mCb = arg1;
        }

        public final boolean onResult(int arg2, String[] arg3, int[] arg4) {
            return  mCb != null ? mCb.onResult(arg2, arg3, arg4): false;
        }
    };
}

