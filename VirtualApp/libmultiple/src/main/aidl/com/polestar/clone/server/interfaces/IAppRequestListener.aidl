// IAppRequestListener.aidl
package com.polestar.clone.server.interfaces;

interface IAppRequestListener {
    void onRequestInstall(in String path);
    void onRequestUninstall(in String pkg);
}
