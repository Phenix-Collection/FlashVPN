package winterfell.flash.vpn.reward.network.datamodels;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.TimeModel;
import com.witter.msg.Sender;

public class VpnRequirement extends TimeModel {
    /**
     * "created_at": "2019-02-20 02:33:51",
     "updated_at": "2019-02-20 02:33:51",
     "from_ip": "183.194.166.134",
     "public_ip": "51.15.118.154",
     "private_ip": "",
     "device_id": "nova1_nova.fast.free.vpn",
     "port": 26312,
     "password": "hzouve9N",
     "limit_speed_normal": -1,
     "limit_speed_vip": -1
     */
    @SerializedName("public_ip")
    public String mPublicIp;
    @SerializedName("port")
    public int mPort;
    @SerializedName("password")
    public String mPassword;
    @SerializedName("limit_speed_normal")
    public int mLimitSpeedNormal;
    @SerializedName("limit_speed_vip")
    public int mLimitSpeedVip;

    //ss://aes-256-cfb:passwd@95.179.225.74:28388
    public String toSSConfig(Context context) {
        return "ss://aes-256-cfb:"+ Sender.Rreceive(context, mPassword) + "@" + mPublicIp
                + ":" + mPort;
    }
}
