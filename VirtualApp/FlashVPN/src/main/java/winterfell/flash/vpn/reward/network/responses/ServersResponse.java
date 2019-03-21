package winterfell.flash.vpn.reward.network.responses;

import com.google.gson.annotations.SerializedName;
import winterfell.flash.vpn.reward.network.datamodels.RegionServers;

import java.util.ArrayList;

public class ServersResponse {
    @SerializedName("regionServers")
    public ArrayList<RegionServers> mVpnServers;

    public ServersResponse() {
        mVpnServers = new ArrayList<>();
    }
}
