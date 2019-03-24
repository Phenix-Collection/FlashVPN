package com.polestar.clone.client.hook.base;

import com.polestar.clone.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * Created by guojia on 2019/3/24.
 */

public class ReplaceUserIdMethodProxy extends StaticMethodProxy {
    private final int index;
    public ReplaceUserIdMethodProxy(String name, int index) {
        super(name);
        this.index = index;
    }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        int uid = (int) args[index];
        if (uid == VUserHandle.myUserId()) {
            args[index] = VUserHandle.getHostUserId();
        }
        return super.beforeCall(who, method, args);
    }
}
