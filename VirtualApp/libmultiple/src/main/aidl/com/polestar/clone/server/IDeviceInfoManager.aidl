// IDeviceInfoManager.aidl
package com.polestar.clone.server;

import com.polestar.clone.remote.VDeviceInfo;

interface IDeviceInfoManager {

    VDeviceInfo getDeviceInfo(int userId);

    void updateDeviceInfo(int userId, in VDeviceInfo info);

}
