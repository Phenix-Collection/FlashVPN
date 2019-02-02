package com.polestar.clone.client.hook.proxies.notification;

import android.os.Build;
import android.os.IInterface;

import com.polestar.clone.client.hook.base.Inject;
import com.polestar.clone.client.hook.base.MethodInvocationProxy;
import com.polestar.clone.client.hook.base.MethodInvocationStub;
import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.helper.compat.BuildCompat;
import com.polestar.clone.helper.utils.VLog;

import java.lang.reflect.Method;

import mirror.android.app.NotificationManager;
import mirror.android.content.pm.ParceledListSlice;
import mirror.android.widget.Toast;

/**
 * @author Lody
 * @see android.app.NotificationManager
 * @see android.widget.Toast
 */
@Inject(MethodProxies.class)
public class NotificationManagerStub extends MethodInvocationProxy<MethodInvocationStub<IInterface>> {

    public NotificationManagerStub() {
        super(new MethodInvocationStub<IInterface>(NotificationManager.getService.call()));
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enqueueToast"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("enqueueToastEx"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("cancelToast"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeAutomaticZenRules"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getImportance"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("areNotificationsEnabled"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationPolicy"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("setNotificationPolicyAccessGranted"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGranted"));
            addMethodProxy(new ReplaceCallingPkgMethodProxy("isNotificationPolicyAccessGrantedForPackage"));
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
            addMethodProxy(new ReplaceCallingPkgMethodProxy("removeEdgeNotification"));
        }
        if(BuildCompat.isOreo()) {
            this.addMethodProxy(new ReplaceCallingPkgMethodProxy("getNotificationChannelForPackage"));
            this.addMethodProxy(new ReplaceCallingPkgMethodProxy("createNotificationChannelsForPackage"));
        }

        if(Build.VERSION.SDK_INT >= 23) {
            this.addMethodProxy(new ReplaceCallingPkgMethodProxy("getAppActiveNotifications"));
            this.addMethodProxy(new ReplaceCallingPkgMethodProxy("getActiveNotifications"));
        }

        this.addMethodProxy(new ReplaceCallingPkgMethodProxy("setInterruptionFilter"));
        this.addMethodProxy(new ReplaceCallingPkgMethodProxy("getPackageImportance"));
    }

    @Override
    public void inject() throws Throwable {
        NotificationManager.sService.set(getInvocationStub().getProxyInterface());
        Toast.sService.set(getInvocationStub().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return NotificationManager.getService.call() != getInvocationStub().getProxyInterface();
    }
}
