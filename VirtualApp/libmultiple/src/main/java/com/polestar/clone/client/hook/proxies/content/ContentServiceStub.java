package com.polestar.clone.client.hook.proxies.content;

import android.os.Build;

import com.polestar.clone.client.hook.base.BinderInvocationProxy;
import com.polestar.clone.client.hook.base.MethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastUserIdMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceUidMethodProxy;
import com.polestar.clone.client.hook.base.ResultStaticMethodProxy;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.os.VUserHandle;

import java.lang.reflect.Method;

import mirror.android.content.ContentResolver;
import mirror.android.content.IContentService;

/**
 * @author Lody
 *
 * @see IContentService
 */

public class ContentServiceStub extends BinderInvocationProxy {

    public ContentServiceStub() {
        super(IContentService.Stub.asInterface, "content");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addMethodProxy(new RegisterContentObserver());
            addMethodProxy(new ResultStaticMethodProxy("notifyChange", null));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("getIsSyncableAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("setMasterSyncAutomaticallyAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("getSyncAdapterTypesAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("getSyncStatusAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("isSyncPendingAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("putCache"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("getCache"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("syncAsUser"));
            addMethodProxy(new ReplaceLastUserIdMethodProxy("cancelSyncAsUser"));
        }
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        ContentResolver.sContentService.set(this.getInvocationStub().getProxyInterface());
    }

    private static class RegisterContentObserver extends MethodProxy {
        @Override
        public String getMethodName() {
            return "registerContentObserver";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            VLog.logbug("RegisterContentObserver", "RegisterContentObserver hooked");
            try {
                if (args[args.length - 1] instanceof  Integer) {
                    args[args.length - 1] = VUserHandle.getHostUserId();
                }
                return super.call(who, method, args);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return null;
            }
//            if ("com.whatsapp".equals(VClientImpl.get().getCurrentPackage())){

//            }else{
//                return super.call(who, method, args);
//            }
        }
    }
}
