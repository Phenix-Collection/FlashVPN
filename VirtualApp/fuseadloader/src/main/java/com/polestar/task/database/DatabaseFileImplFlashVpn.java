package com.polestar.task.database;

import android.content.Context;

public class DatabaseFileImplFlashVpn extends DatabaseFileImpl {

    private DatabaseFileImplFlashVpn(Context context) {
        super(context);
    }

    @Override
    public boolean isDataAvailable() {
        return mUser != null;
    }

    public synchronized static DatabaseApi getDatabaseFileImplFlashVpn(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseFileImplFlashVpn(context);
        }
        return sInstance;
    }
}
