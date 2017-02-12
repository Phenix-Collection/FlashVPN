package com.lody.virtual.client.ipc;

import android.os.Binder;
import android.os.DeadObjectException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 */

public class LocalProxyUtils {

    /**
     * Generates the Proxy instance for a base object, each IPC call will clean its calling identity.
     * @param interfaceClass interface class
     * @param base base object
     * @return proxy object
     */
    public static <T> T genProxy(Class<T> interfaceClass, final Object base, final DeadServerHandler handler) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{ interfaceClass }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                long identity = Binder.clearCallingIdentity();
                try {
                    return method.invoke(base, args);
                } catch (Throwable e) {
                    if (e.getCause() instanceof DeadObjectException) {
                        ServiceManagerNative.clearServerFetcher();
                        if (handler != null) {
                            Object newBase = handler.getNewRemoteInterface();
                            if (newBase != null) {
                                try {
                                    return method.invoke(newBase, args);
                                } catch (Throwable retry_e) {
                                    throw retry_e.getCause() != null ? retry_e.getCause() : retry_e;
                                }
                            }
                            throw e.getCause();
                        }
                    }
                    throw e.getCause() != null ? e.getCause() : e;
                }finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        });
    }

    public interface DeadServerHandler {
        Object getNewRemoteInterface();
    }
}
