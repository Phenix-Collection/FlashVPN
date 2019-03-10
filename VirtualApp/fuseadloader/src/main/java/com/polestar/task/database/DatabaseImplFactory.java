package com.polestar.task.database;

import android.content.Context;

public class DatabaseImplFactory {
    public static final String TARGET_FLASH_VPN = "flashVpn";

    public static DatabaseApi getDatabaseApi(Context context) {
        return DatabaseFileImpl.getDatabaseFileImpl(context);
    }

    public static DatabaseApi getDatabaseApi(Context context, String target) {
        if (TARGET_FLASH_VPN.equals(target)) {
            return DatabaseFileImplFlashVpn.getDatabaseFileImplFlashVpn(context);
        } else {
            return DatabaseFileImpl.getDatabaseFileImpl(context);
        }
    }
}
