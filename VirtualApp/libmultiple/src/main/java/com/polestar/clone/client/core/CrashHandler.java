package com.polestar.clone.client.core;

/**
 * @author Lody
 */

public interface CrashHandler {

    void handleUncaughtException(Thread t, Throwable e);

}
