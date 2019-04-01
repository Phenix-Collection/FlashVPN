package com.polestar.clone.client.hook.base;


import android.os.Build;

import com.polestar.clone.helper.utils.ArrayUtils;
import com.polestar.clone.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * Created by guojia on 2019/3/24.
 */

public class ReplaceLastUserIdMethodProxy extends StaticMethodProxy {
    public ReplaceLastUserIdMethodProxy(String name) {
        super(name);
    }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int index = ArrayUtils.indexOfLast(args, Integer.class);
            if (index != -1) {
                int userId = (int) args[index];
                if (userId == VUserHandle.myUserId()) {
                    args[index] = VUserHandle.getHostUserId();
                }
            }
        }
        return super.beforeCall(who, method, args);
    }
}
