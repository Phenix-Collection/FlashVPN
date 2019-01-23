package com.polestar.task.database;

import android.content.Context;

public class DatabaseImplFactory {
    public static DatabaseApi getDatabaseApi(Context context) {
        return DatabaseFileImpl.getDatabaseFileImpl(context);
    }
}
