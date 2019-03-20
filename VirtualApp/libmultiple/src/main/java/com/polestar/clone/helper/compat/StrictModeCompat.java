package com.polestar.clone.helper.compat;

import mirror.android.os.StrictMode;

/**
 * Created by guojia on 2019/3/20.
 */

public class StrictModeCompat {
    public static int DETECT_VM_FILE_URI_EXPOSURE;
    public static int PENALTY_DEATH_ON_FILE_URI_EXPOSURE;

    static {
        int v0 = StrictMode.DETECT_VM_FILE_URI_EXPOSURE == null ? 8192 : StrictMode.DETECT_VM_FILE_URI_EXPOSURE.get();
        StrictModeCompat.DETECT_VM_FILE_URI_EXPOSURE = v0;
        v0 = StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE == null ? 67108864 : StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE.get();
        StrictModeCompat.PENALTY_DEATH_ON_FILE_URI_EXPOSURE = v0;
    }

    public StrictModeCompat() {
        super();
    }

    public static boolean disableDeathOnFileUriExposure() {
        boolean v0 = true;
        try {
            StrictMode.disableDeathOnFileUriExposure.call(new Object[0]);
        }
        catch(Throwable v2) {
            try {
                StrictMode.sVmPolicyMask.set(StrictMode.sVmPolicyMask.get() & ((StrictModeCompat.DETECT_VM_FILE_URI_EXPOSURE | StrictModeCompat.PENALTY_DEATH_ON_FILE_URI_EXPOSURE) ^ -1));
            }
            catch(Throwable v0_1) {
                v0_1.printStackTrace();
                v0 = false;
            }
        }

        return v0;
    }
}

