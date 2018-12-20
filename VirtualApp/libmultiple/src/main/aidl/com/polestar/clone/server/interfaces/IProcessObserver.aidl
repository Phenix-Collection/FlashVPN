// IProcessObserver.aidl
package com.polestar.clone.server.interfaces;

interface IProcessObserver {
    void onProcessCreated(in String pkg, in String processName);

    void onProcessDied(in String pkg, in String processName);
}
