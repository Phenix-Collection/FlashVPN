package com.polestar.clone.helper.compat;

import android.os.Build;
import android.text.TextUtils;

/**
 * @author Lody
 */

public class BuildCompat {

    public BuildCompat() {
        super();
    }

    public static int getPreviewSDKInt() {
        if(Build.VERSION.SDK_INT >= 23) {
            try {
                int v0_1 = Build.VERSION.PREVIEW_SDK_INT;
                return v0_1;
            }
            catch(Throwable v0) {
            }
        }

        return 0;
    }

    public static boolean is360UI() {
        String v0 = SystemPropertiesCompat.get("ro.build.uiversion", "");
        boolean v0_1 = v0 == null || !v0.toUpperCase().contains("360UI") ? false : true;
        return v0_1;
    }

    public static boolean isColorOS() {
        String s = SystemPropertiesCompat.get("ro.build.version.opporom", "") ;

        //|| SystemPropertiesCompat.get("ro.rom.different.version", "") != null ? true : false;
        return !TextUtils.isEmpty(s);
    }

    public static boolean isEMUI() {
        boolean v0 = true;
        if(!Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            String v1 = SystemPropertiesCompat.get("ro.build.version.emui", "");
            if(v1 != null && (v1.contains("EmotionUI"))) {
                return v0;
            }

            v0 = false;
        }

        return v0;
    }

    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    public static boolean isLetv() {
        return Build.MANUFACTURER.equalsIgnoreCase("Letv");
    }

    public static boolean isMIUI() {
        return  !TextUtils.isEmpty(
                SystemPropertiesCompat.get("ro.miui.ui.version.code", "") );
    }

    public static boolean isOreo() {
        return (Build.VERSION.SDK_INT == 25 && getPreviewSDKInt() > 0)
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isPie() {

        return Build.VERSION.SDK_INT >= 28;
    }

    public static boolean isSamsung() {
        boolean v0 = ("samsung".equalsIgnoreCase(Build.BRAND)) || ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) ? true : false;
        return v0;
    }

    public static boolean isVivo() {
        boolean v0 = SystemPropertiesCompat.get("ro.vivo.os.build.display.id", "") != null
                || SystemPropertiesCompat.get("ro.vivo.os.version", "") != null ? true : false;
        return v0;
    }
}