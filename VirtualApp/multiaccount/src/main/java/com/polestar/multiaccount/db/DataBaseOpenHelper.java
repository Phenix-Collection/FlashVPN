package com.polestar.multiaccount.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.polestar.multiaccount.utils.MLogs;

/**
 * Created by yxx on 2016/7/27.
 */
public class DataBaseOpenHelper extends DaoMaster.DevOpenHelper{
    public DataBaseOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        MLogs.logBug("database onUpgrade, oldversion: " + oldVersion + ", newVersion: " + newVersion);
        MLogs.e(new Exception("onUpgrade test"));
        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 1:
                    upgradeDatabaseFrom1to2(db);
                    break;
                default:
                    break;
            }
        }
    }

    private void upgradeDatabaseFrom1to2(SQLiteDatabase db) {
        MLogs.logBug("upgradeDatabaseFrom1to2");
        db.execSQL("ALTER TABLE " + AppModelDao.TABLENAME + " ADD COLUMN " + AppModelDao.Properties.LockerState.columnName);
    }
}
