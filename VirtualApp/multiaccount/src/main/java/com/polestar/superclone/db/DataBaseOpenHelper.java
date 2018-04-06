package com.polestar.superclone.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;

import org.greenrobot.greendao.database.Database;

/**
 * Created by yxx on 2016/7/27.
 */
public class DataBaseOpenHelper extends DaoMaster.DevOpenHelper{
    public DataBaseOpenHelper(Context context, String name) {
        super(context, name);
    }
    private Context mContext;

    public DataBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
    //    super.onUpgrade(db, oldVersion, newVersion);
        MLogs.logBug("database onUpgrade, oldversion: " + oldVersion + ", newVersion: " + newVersion);
        MLogs.e(new Exception("onUpgrade test"));
        try {
            for (int i = oldVersion; i < newVersion; i++) {
                switch (i) {
                    case 1:
                        upgradeDatabaseFrom1to2(db);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            EventReporter.keyLog(mContext, "DB_UPGRADE", "Failed. Drop all.");
            DaoMaster.dropAllTables(db, true);
            onCreate(db);
        }

    }

    private void upgradeDatabaseFrom1to2(Database db) {
        MLogs.logBug("upgradeDatabaseFrom1to2");
        db.execSQL("ALTER TABLE " + AppModelDao.TABLENAME + " ADD COLUMN " + AppModelDao.Properties.LockerState.columnName);
        Cursor c = db.rawQuery("select * from " + AppModelDao.TABLENAME, null);
        while (c.moveToNext()) {
            String cid = c.getString(c.getColumnIndex( AppModelDao.Properties.Id.columnName));
            ContentValues values = new ContentValues();
            // set default value
            values.put(AppModelDao.Properties.LockerState.columnName, 0);
            MLogs.d("update cid: " + cid);
            SQLiteDatabase raw = (SQLiteDatabase) db.getRawDatabase();
            raw.update(AppModelDao.TABLENAME, values, AppModelDao.Properties.Id.columnName + " =? ", new String[]{cid});
        }
    }
}
