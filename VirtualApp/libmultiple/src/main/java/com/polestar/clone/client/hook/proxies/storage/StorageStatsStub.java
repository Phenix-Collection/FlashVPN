package com.polestar.clone.client.hook.proxies.storage;

import android.annotation.TargetApi;
import android.app.usage.StorageStats;

import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.client.hook.utils.MethodParameterUtils;
import com.polestar.clone.helper.utils.ArrayUtils;
import com.polestar.clone.os.VUserHandle;

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
        addMethodProxy(new ReplaceLastPkgMethodProxy("getTotalBytes"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("isQuotaSupported"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheBytes"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getCacheQuotaBytes") {
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    if (args.length == 3 && args[args.length - 2] instanceof Integer) {
                        args[args.length - 2] = VirtualCore.get().myUid();
                    }
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return 0;
                }
            }
        });
        addMethodProxy(new QueryStatsForPackage("queryStatsForPackage"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUid"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    if (args.length == 3 && args[args.length - 2] instanceof Integer) {
                        args[args.length - 2] = VirtualCore.get().myUid();
                    }
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return 0;
                }
            }
        });
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryStatsForUser"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    if (args.length == 3 && args[args.length - 2] instanceof Integer) {
                        args[args.length - 2] = VUserHandle.getHostUserId();
                    }
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return 0;
                }
            }
        });
        addMethodProxy(new ReplaceLastPkgMethodProxy("queryExternalStatsForUser"){
            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                try {
                    if (args.length == 3 && args[args.length - 2] instanceof Integer) {
                        args[args.length - 2] = VUserHandle.getHostUserId();
                    }
                    return super.call(who, method, args);
                } catch (Throwable ex) {
                    return 0;
                }
            }
        });
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
                try {
                    return super.call(arg4, arg5, arg6);
                } catch (Throwable ex) {
                    return queryStatsForPackage((String)arg6[v0], (int)arg6[v1]);
                }
            }
            else {
                return queryStatsForPackage((String)arg6[v0], (int)arg6[v1]);
            }
        }
    }
}
