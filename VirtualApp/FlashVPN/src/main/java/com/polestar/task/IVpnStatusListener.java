package com.polestar.task;

import com.polestar.task.network.datamodels.VpnRequirement;
import com.polestar.task.network.responses.ServersResponse;

public interface IVpnStatusListener extends IGeneralErrorListener {

    void onAcquireSucceed(VpnRequirement requirement);
    void onAcquireFailed(String publicIp, ADErrorCode code);

    void onReleaseSucceed(String publicIp);
    void onReleaseFailed(String publicIp, ADErrorCode code);

    void onGetAllServers(ServersResponse servers);
}