package com.lody.virtual.client.hook.proxies.am;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class IUpdateEngine {
    public static Class<?> TYPE = RefClass.load(IUpdateEngine.class, "android.os.IUpdateEngine");

    public static class  Stub {
        public static Class<?> TYPE = RefClass.load(IUpdateEngine.Stub.class, "android.os.IUpdateEngine");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
