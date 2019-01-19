package com.polestar.clone.client.hook.providers;

/**
 * Created by guojia on 2019/1/19.
 */


import android.os.Bundle;

import com.polestar.clone.client.VClientImpl;
import com.polestar.clone.client.hook.base.MethodBox;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.os.VUserHandle;
import com.polestar.clone.remote.BadgerInfo;

import java.lang.reflect.InvocationTargetException;

public class BadgeProviderHook extends ExternalProviderHook {
    public BadgeProviderHook(Object arg1) {
        super(arg1);
    }

    public Bundle call(MethodBox arg4, String arg5, String arg6, Bundle arg7) throws InvocationTargetException {
        Bundle v0_1;
        BadgerInfo v0;
        if("change_badge".equals(arg5)) {
            v0 = new BadgerInfo();
            v0.userId = VUserHandle.myUserId();
            v0.packageName = arg7.getString("package");
            v0.className = arg7.getString("class");
            v0.badgerCount = arg7.getInt("badgenumber");
            VActivityManager.get().notifyBadgerChange(v0);
            v0_1 = new Bundle();
            v0_1.putBoolean("success", true);
        }
        else {
            if("setAppBadgeCount".equals(arg5)) {
                v0 = new BadgerInfo();
                v0.userId = VUserHandle.myUserId();
                v0.packageName = VClientImpl.get().getCurrentPackage();
                v0.badgerCount = arg7.getInt("app_badge_count");
                VActivityManager.get().notifyBadgerChange(v0);
                new Bundle().putBoolean("success", true);
            }

            v0_1 = super.call(arg4, arg5, arg6, arg7);
        }

        return v0_1;
    }
}
