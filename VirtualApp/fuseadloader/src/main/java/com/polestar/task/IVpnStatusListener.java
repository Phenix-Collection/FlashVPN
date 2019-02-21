package com.polestar.task;

import com.polestar.task.network.datamodels.VpnRequirement;
import com.polestar.task.network.datamodels.VpnServer;

import java.util.ArrayList;

public interface IVpnStatusListener extends IGeneralErrorListener {

    void onAcquireSucceed(VpnRequirement requirement);
    void onAcquireFailed(String publicIp, ADErrorCode code);

    void onReleaseSucceed(String publicIp);
    void onReleaseFailed(String publicIp, ADErrorCode code);

    void onGetAllServers(ArrayList<VpnServer> servers);
}
