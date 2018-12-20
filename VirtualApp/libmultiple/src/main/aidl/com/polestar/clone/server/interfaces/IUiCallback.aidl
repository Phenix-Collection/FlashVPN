// IUiCallback.aidl
package com.polestar.clone.server.interfaces;

interface IUiCallback {
    void onAppOpened(in String packageName, in int userId);
}
