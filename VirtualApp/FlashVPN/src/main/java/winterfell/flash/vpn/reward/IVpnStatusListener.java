package winterfell.flash.vpn.reward;

import com.polestar.task.ADErrorCode;
import com.polestar.task.IGeneralErrorListener;

import winterfell.flash.vpn.reward.network.datamodels.VpnRequirement;
import winterfell.flash.vpn.reward.network.responses.ServersResponse;

public interface IVpnStatusListener extends IGeneralErrorListener {

    void onAcquireSucceed(VpnRequirement requirement);
    void onAcquireFailed(String publicIp, ADErrorCode code);

    void onReleaseSucceed(String publicIp);
    void onReleaseFailed(String publicIp, ADErrorCode code);

    void onGetAllServers(ServersResponse servers);
}