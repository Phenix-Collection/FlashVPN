package com.polestar.superclone.db;

import android.content.Context;

import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.pbinterface.DataObserver;
import com.polestar.superclone.pbinterface.PbObservable;
import com.polestar.superclone.utils.MLogs;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
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

    public static void resetSession() {
        daoSession = null;
    }

    public static List<AppModel> queryAppList(Context context) {
        AppModelDao appModelDao = getDaoSession(context).getAppModelDao();
        List<AppModel> list = appModelDao.queryBuilder().orderAsc(AppModelDao.Properties.Index).list();
        if (list != null)
            Collections.sort(list, new Comparator<AppModel>() {
                @Override
                public int compare(AppModel o1, AppModel o2) {
                    if (o1 == o2)
                        return 0;
                    if (o1 == null || o2 == null) {
                        return o1 == null ? -1 : 1;
                    }
                    return o1.getOriginalIndex() - o2.getOriginalIndex();
                }
            });
        return list;
    }

    public static List<AppModel> queryAppModelByPackageName(Context context, String packageName) {
        AppModelDao appModelDao = getDaoSession(context).getAppModelDao();
        return appModelDao.queryBuilder().where(AppModelDao.Properties.PackageName.eq(packageName)).list();
    }

    public static AppModel queryAppModelByPackageName(Context context, String packageName, int userId) {
        AppModelDao appModelDao = getDaoSession(context).getAppModelDao();
        List<AppModel> list = appModelDao.queryBuilder().where(AppModelDao.Properties.PackageName.eq(packageName)).list();
        if (list != null) {
            for (AppModel cm : list) {
                if (cm.getPkgUserId() == userId)
                    return cm;
            }
        }
        return null;
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
