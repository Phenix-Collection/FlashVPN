package in.dualspace.cloner.db;

import android.content.Context;

import in.dualspace.cloner.utils.MLogs;

import org.greenrobot.greendao.database.Database;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by DualApp on 2017/7/16.
 */

public class DBManager {
    private static DaoSession daoSession;
    private static final String NEW_DB_NAME = "incloner.db";

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
        List<CloneModel> list = appModelDao.queryBuilder().orderAsc(CloneModelDao.Properties.Index).list();
        if (list != null)
            Collections.sort(list, new Comparator<CloneModel>() {
                @Override
                public int compare(CloneModel o1, CloneModel o2) {
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

    public static List<CloneModel> queryCloneModelByPackageName(Context context, String packageName) {
        CloneModelDao appModelDao = getDaoSession(context).getCloneModelDao();
        return appModelDao.queryBuilder().where(CloneModelDao.Properties.PackageName.eq(packageName)).list();
    }

    public static CloneModel queryCloneModelByPackageName(Context context, String packageName, int userId) {
        CloneModelDao appModelDao = getDaoSession(context).getCloneModelDao();
        List<CloneModel> list = appModelDao.queryBuilder().where(CloneModelDao.Properties.PackageName.eq(packageName)).list();
        if (list != null) {
            for (CloneModel cm : list) {
                if (cm.getPkgUserId() == userId)
                    return cm;
            }
        }
        return null;
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
