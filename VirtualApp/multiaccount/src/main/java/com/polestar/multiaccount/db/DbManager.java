package com.polestar.multiaccount.db;

import android.content.Context;

import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.pbinterface.DataObserver;
import com.polestar.multiaccount.pbinterface.PbObservable;
import com.polestar.multiaccount.utils.MLogs;

import java.io.File;
import java.util.List;

public class DbManager {

    private static DaoSession daoSession;
    private static final String DB_NAME = "DotSpaceDb";
    private static final String NEW_DB_NAME = "appclone.db";

    private static boolean isRenamed = false;
    private static void renameDB(Context context) {
        try {
            File old  = context.getDatabasePath(DB_NAME);
            if (old.exists()) {
                File oldJournal = new File(old.getPath()+"-journal");
                File newJournal = new File(context.getDatabasePath(NEW_DB_NAME).getPath() + "-journal");
                old.renameTo(context.getDatabasePath(NEW_DB_NAME));
                oldJournal.renameTo(newJournal);
                MLogs.logBug("Renamed db file");
            }
        } catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        isRenamed = true;
    }

    public static synchronized DaoSession getDaoSession(Context context) {
        if (!isRenamed) {
            renameDB(context);
        }
        if (daoSession == null) {
            daoSession = new DaoMaster(new DataBaseOpenHelper(context, NEW_DB_NAME).getWritableDb()).newSession();
        }
        return daoSession;
    }

    public static List<AppModel> queryAppList(Context context) {
        AppModelDao appModelDao = getDaoSession(context).getAppModelDao();
        return appModelDao.queryBuilder().orderAsc(AppModelDao.Properties.Index).list();
    }

    public static List<AppModel> queryAppModelByPackageName(Context context, String packageName) {
        AppModelDao appModelDao = getDaoSession(context).getAppModelDao();
        return appModelDao.queryBuilder().where(AppModelDao.Properties.PackageName.eq(packageName)).list();
    }

    public static void insertAppModel(Context context, AppModel appModel) {
        AppModelDao AppModelDao = getDaoSession(context).getAppModelDao();
        AppModelDao.insert(appModel);
    }

    public static void deleteAppModel(Context context, AppModel appModel) {
        AppModelDao AppModelDao = getDaoSession(context).getAppModelDao();
        AppModelDao.delete(appModel);
    }

    public static void updateAppModel(Context context, AppModel appModel) {
        AppModelDao AppModelDao = getDaoSession(context).getAppModelDao();
        AppModelDao.update(appModel);
    }

    public static void updateAppModelList(Context context, List<AppModel> models) {
        AppModelDao AppModelDao = getDaoSession(context).getAppModelDao();
        AppModelDao.updateInTx(models);
    }

    public static void deleteAppModeList(Context context, List<AppModel> models) {
        AppModelDao AppModelDao = getDaoSession(context).getAppModelDao();
        AppModelDao.deleteInTx(models);
    }

    /*
        manage observers for this DB, when database updated, you may need notify the observers manually.
     */
    private static PbObservable sPbObservable = new PbObservable();

    public static void registerObserver(DataObserver observer) {
        sPbObservable.registerObserver(observer);
    }

    public static void unregisterObserver(DataObserver observer) {
        sPbObservable.unregisterObserver(observer);
    }

    public static void notifyChanged() {
        sPbObservable.notifyChanged();
    }

    public static void nofityInvalidated() {
        sPbObservable.notifyInvalidated();
    }
}
