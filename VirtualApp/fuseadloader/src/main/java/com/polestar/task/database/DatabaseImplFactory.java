package com.polestar.task.database;

import android.content.Context;

public class DatabaseImplFactory {

    public static boolean CONF_NEED_TASK = true;
    public static boolean CONF_NEED_PRODUCT = true;

    public static DatabaseApi getDatabaseApi(Context context) {
        return DatabaseFileImpl.getDatabaseFileImpl(context, CONF_NEED_TASK, CONF_NEED_PRODUCT);
    }
}
