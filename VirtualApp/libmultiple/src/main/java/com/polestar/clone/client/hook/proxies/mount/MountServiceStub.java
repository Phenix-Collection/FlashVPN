package com.polestar.clone.client.hook.proxies.mount;

import android.os.IInterface;

import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceUidMethodProxy;
import com.polestar.clone.helper.compat.BuildCompat;

import java.lang.reflect.Method;

import mirror.RefStaticMethod;
import mirror.android.os.mount.IMountService;
import mirror.android.os.storage.IStorageManager;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class MountServiceStub extends BinderInvocationProxy {

    public MountServiceStub() {
        super(getInterfaceMethod(), "mount");
    }

    private static RefStaticMethod<IInterface> getInterfaceMethod() {
        if (BuildCompat.isOreo()) {
            return IStorageManager.Stub.asInterface;
        } else {
            return IMountService.Stub.asInterface;
        }

        //getCacheSizeBytes
        //getCacheQuotaBytes(convert(storageUuid), app.uid)

//        long getCacheQuotaBytes(String volumeUuid, int uid) = 75;
//        295    long getCacheSizeBytes(String volumeUuid, int uid) = 76;
//        296    long getAllocatableBytes(String volumeUuid, int flags, String callingPackage) = 77;
    }


    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceLastPkgMethodProxy("getAllocatableBytes"));
        addMethodProxy(new ReplaceUidMethodProxy("getCacheSizeBytes", 1));
        addMethodProxy(new ReplaceUidMethodProxy("getCacheQuotaBytes", 1));
    }
}
