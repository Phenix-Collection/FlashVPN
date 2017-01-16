package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by guojia on 2017/1/14.
 */
class RefContentProvider extends Hook {
    private final static String TAG = "RefContentProvider";
    @Override
    public String getName() {
        return "refContentProvider";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        if (args[0] == null) {
            VLog.logbug(TAG, "connection is null. return");
            return false;
        }else{
            try{
                return method.invoke(who, args);
            }catch (InvocationTargetException e){
                VLog.logbug(TAG, VLog.getStackTraceString(e));
                return false;
            }
        }
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}