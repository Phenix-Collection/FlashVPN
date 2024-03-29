package com.polestar.superclone.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.polestar.superclone.model.AppModel;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig appModelDaoConfig;

    private final AppModelDao appModelDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        appModelDaoConfig = daoConfigMap.get(AppModelDao.class).clone();
        appModelDaoConfig.initIdentityScope(type);

        appModelDao = new AppModelDao(appModelDaoConfig, this);

        registerDao(AppModel.class, appModelDao);
    }
    
    public void clear() {
        appModelDaoConfig.getIdentityScope().clear();
    }

    public AppModelDao getAppModelDao() {
        return appModelDao;
    }

}
