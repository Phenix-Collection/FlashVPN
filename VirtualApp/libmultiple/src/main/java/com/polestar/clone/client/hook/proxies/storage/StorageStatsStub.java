package com.polestar.clone.client.hook.proxies.storage;

import android.annotation.TargetApi;
import android.app.usage.StorageStats;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

import mirror.android.app.usage.IStorageStatsManager;

/**
 * Created by guojia on 2019/3/2.
 */

@TargetApi(value=26) public class StorageStatsStub extends BinderInvocationProxy {
    public StorageStatsStub() {
        super(IStorageStatsManager.Stub.TYPE, "storagestats");
    }

    protected void onBindMethods() {
        super.onBindMethods();
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("getTotalBytes"));
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheBytes"));
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheQuotaBytes"));
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUser"));
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("queryExternalStatsForUser"));
        this.addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUid"));
        this.addMethodProxy(new QueryStatsForPackage("queryStatsForPackage"));
    }

    private StorageStats queryStatsForPackage(String arg5, int arg6) {

        Object v0 = mirror.android.app.usage.StorageStats.ctor.newInstance();
        mirror.android.app.usage.StorageStats.cacheBytes.set(v0, 0);
        mirror.android.app.usage.StorageStats.codeBytes.set(v0, 0);
        mirror.android.app.usage.StorageStats.dataBytes.set(v0, 0);
        return ((StorageStats)v0);
    }

    class QueryStatsForPackage extends StaticMethodProxy {
        QueryStatsForPackage( String arg2) {
            super(arg2);
        }



        @Override
        public Object call(Object arg4, Method arg5, Object[] arg6) throws Throwable {
            StorageStats v0_1;
            int v0 = ArrayUtils.indexOfFirst(arg6, String.class);
            int v1 = ArrayUtils.indexOfLast(arg6, Integer.class);
            if(v0 == -1 || v1 == -1) {
                return super.call(arg4, arg5, arg6);
            }
            else {
                return queryStatsForPackage((String)arg6[v0], (int)arg6[v1]);
            }
        }
    }
}
