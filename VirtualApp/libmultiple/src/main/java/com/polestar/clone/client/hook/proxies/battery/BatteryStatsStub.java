package com.polestar.clone.client.hook.proxies.battery;

import android.annotation.TargetApi;
import android.content.Context;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastUidMethodProxy;

import java.lang.reflect.Method;

import mirror.android.os.health.SystemHealthManager;
import mirror.com.android.internal.app.IBatteryStats;

@TargetApi(value=24) public class BatteryStatsStub extends BinderInvocationProxy {
    private static final String SERVICE_NAME = "batterystats";

    public BatteryStatsStub() {
        super(IBatteryStats.Stub.asInterface, "batterystats");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        if(SystemHealthManager.mBatteryStats != null) {
            SystemHealthManager.mBatteryStats.set(VirtualCore.get().getContext().getSystemService(Context.SYSTEM_HEALTH_SERVICE),
                    this.getInvocationStub().getProxyInterface());
        }
    }

    protected void onBindMethods() {
        super.onBindMethods();
        this.addMethodProxy(new TakeUidSnapshot("takeUidSnapshot"));
    }

    class TakeUidSnapshot extends ReplaceLastUidMethodProxy {
        TakeUidSnapshot( String arg2) {
            super(arg2);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            try {
                return super.call(who, method, args);
            }catch (Throwable ex){
                return null;
            }

        }
    }
}