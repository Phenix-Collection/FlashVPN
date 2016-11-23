package com.polestar.multiaccount.db;

import android.content.Context;

import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.pbinterface.DataObserver;
import com.polestar.multiaccount.pbinterface.PbObservable;

import java.util.List;

/**
 * Created by yxx on 2016/7/21.
 */
public class DbManager {

    private static DaoSession daoSession;
    private static final String DB_NAME = "DotSpaceDb";

    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            daoSession = new DaoMaster(new DataBaseOpenHelper(context, DB_NAME).getWritableDb()).newSession();
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
