package com.polestar.domultiple.db;

import android.content.Context;

import com.polestar.domultiple.utils.MLogs;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.util.List;

/**
 * Created by PolestarApp on 2017/7/16.
 */

public class DBManager {
    private static DaoSession daoSession;
    private static final String NEW_DB_NAME = "do-multiple.db";

    public static synchronized DaoSession getDaoSession(Context context) {
        DaoMaster.OpenHelper openHelper = new DaoMaster.DevOpenHelper(context, NEW_DB_NAME) {
            @Override
            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                super.onUpgrade(db, oldVersion, newVersion);
                MLogs.logBug("Not support db upgrade from : " + oldVersion + " to " + newVersion);
            }
        };
        if (daoSession == null) {
            daoSession = new DaoMaster(openHelper.getWritableDb()).newSession();
        }
        return daoSession;
    }

    public static void resetSession() {
        daoSession = null;
    }

    public static List<CloneModel> queryAppList(Context context) {
        CloneModelDao appModelDao = getDaoSession(context).getCloneModelDao();
        return appModelDao.queryBuilder().orderAsc(CloneModelDao.Properties.Index).list();
    }

    public static List<CloneModel> queryCloneModelByPackageName(Context context, String packageName) {
        CloneModelDao appModelDao = getDaoSession(context).getCloneModelDao();
        return appModelDao.queryBuilder().where(CloneModelDao.Properties.PackageName.eq(packageName)).list();
    }

    public static void insertCloneModel(Context context, CloneModel appModel) {
        CloneModelDao CloneModelDao = getDaoSession(context).getCloneModelDao();
        CloneModelDao.insert(appModel);
    }

    public static void deleteCloneModel(Context context, CloneModel appModel) {
        CloneModelDao CloneModelDao = getDaoSession(context).getCloneModelDao();
        CloneModelDao.delete(appModel);
    }

    public static void updateCloneModel(Context context, CloneModel appModel) {
        CloneModelDao CloneModelDao = getDaoSession(context).getCloneModelDao();
        CloneModelDao.update(appModel);
    }

    public static void updateCloneModelList(Context context, List<CloneModel> models) {
        CloneModelDao CloneModelDao = getDaoSession(context).getCloneModelDao();
        CloneModelDao.updateInTx(models);
    }

    public static void deleteAppModeList(Context context, List<CloneModel> models) {
        CloneModelDao CloneModelDao = getDaoSession(context).getCloneModelDao();
        CloneModelDao.deleteInTx(models);
    }
}
