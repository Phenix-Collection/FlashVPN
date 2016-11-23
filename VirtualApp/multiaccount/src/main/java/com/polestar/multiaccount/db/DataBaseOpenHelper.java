package com.polestar.multiaccount.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
    }
}
