package winterfell.flash.vpn.reward.network.datamodels;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.witter.msg.Sender;

import java.util.HashMap;

import winterfell.flash.vpn.R;
import winterfell.flash.vpn.utils.MLogs;

public class VpnServer  {

    @SerializedName("public_ip")
    public String mPublicIp;

    @SerializedName("geo")
    public String mGeo;

    @SerializedName("city")
    public String mCity;

    @SerializedName("pri")
    public int mPri;

    @SerializedName("vip")
    public int mVip;

    @SerializedName("is_online")
    public int mIsOnline;
//ss://aes-256-cfb:passwd@95.179.225.74:28388

    @SerializedName("pingport")
    public String mPingport;

    //本地的性能数据如下
    @SerializedName("ping")
    public int mPingDelayMilli;
    @SerializedName("connect")
    public int mConnectDelayMilli;
    @SerializedName("downloadSpeed")
    public int mByteDownPs;
    @SerializedName("uploadSpeed")
    public int mByteUpPs;

    public String toSSPingConfig(Context context) {
        if (mPingport != null && !mPingport.isEmpty()) {
            String p = "";
            String po = "";
            try {
                String[] pin = mPingport.split("_");
                po = pin[0];
                p = Sender.Rreceive(context, pin[1]);
            } catch (Exception e) {
                return "";
            }
            return "ss://aes-256-cfb:"+ p + "@" + mPublicIp
                    + ":" + po;
        } else {
            return "";
        }
    }

    public static int PRIORITY_DEFAULT = 5;
    public static int NO_PING = 3000; //1
    public static int PING_TIMEOUT = 5000; //0
    public static int LEVEL_0_PING = PING_TIMEOUT; //1
    public static int LEVEL_1_PING = NO_PING; //1
    public static int LEVEL_2_PING = 1000;
    public static int LEVEL_3_PING = 500;
    public static int LEVEL_4_PING = 200;
    public int priority = PRIORITY_DEFAULT;

    public static final int SERVER_ID_AUTO = -1;

    public void dump() {
        MLogs.i(mPublicIp + " " + mPingDelayMilli);
    }

    private static HashMap<String, Integer> sFlagResMap = new HashMap<>();
    static {
        sFlagResMap.put("ae", R.drawable.flag_ae);
        sFlagResMap.put("ar", R.drawable.flag_ar);
        sFlagResMap.put("au", R.drawable.flag_au);
        sFlagResMap.put("bg", R.drawable.flag_bg);
        sFlagResMap.put("br", R.drawable.flag_br);
        sFlagResMap.put("ca", R.drawable.flag_ca);
        sFlagResMap.put("ch", R.drawable.flag_ch);
        sFlagResMap.put("cn", R.drawable.flag_cn);
        sFlagResMap.put("cz", R.drawable.flag_cz);
        sFlagResMap.put("de", R.drawable.flag_de);
        sFlagResMap.put("dz", R.drawable.flag_dz);
        sFlagResMap.put("es", R.drawable.flag_es);
        sFlagResMap.put("fi", R.drawable.flag_fi);
        sFlagResMap.put("fr", R.drawable.flag_fr);
        sFlagResMap.put("gb", R.drawable.flag_gb);
        sFlagResMap.put("hk", R.drawable.flag_hk);
        sFlagResMap.put("ie", R.drawable.flag_ie);
        sFlagResMap.put("in", R.drawable.flag_in);
        sFlagResMap.put("it", R.drawable.flag_it);
        sFlagResMap.put("jp", R.drawable.flag_jp);
        sFlagResMap.put("kr", R.drawable.flag_kr);
        sFlagResMap.put("lu", R.drawable.flag_lu);
        sFlagResMap.put("my", R.drawable.flag_my);
        sFlagResMap.put("nl", R.drawable.flag_nl);
        sFlagResMap.put("pl", R.drawable.flag_pl);
        sFlagResMap.put("ro", R.drawable.flag_ro);
        sFlagResMap.put("ru", R.drawable.flag_ru);
        sFlagResMap.put("sa", R.drawable.flag_sa);
        sFlagResMap.put("se", R.drawable.flag_se);
        sFlagResMap.put("sg", R.drawable.flag_sg);
        sFlagResMap.put("tw", R.drawable.flag_tw);
        sFlagResMap.put("ua", R.drawable.flag_ua);
        sFlagResMap.put("us", R.drawable.flag_us);
        sFlagResMap.put("vn", R.drawable.flag_vn);
        sFlagResMap.put("za", R.drawable.flag_za);
    }

    public int getFlagResId() {
        return getFlagResId(mGeo);
    }

    public static int getFlagResId(String country) {
        String s = country.toLowerCase();
        Integer id = sFlagResMap.get(s);
        if (id == null) {
            return  -1;
        } else {
            return id;
        }
    }

    public int getSignalResId() {
        if (mPingDelayMilli == 0) { //刚刚从服务器拿到，还没ping过 TODO
          mPingDelayMilli = LEVEL_3_PING;
        }

        if (mPingDelayMilli >= LEVEL_0_PING) {
            return getSignalResId(0);
        } else if (mPingDelayMilli >= LEVEL_1_PING) {
            return getSignalResId(1);
        } else if (mPingDelayMilli >= LEVEL_2_PING) {
            return getSignalResId(2);
        }else if (mPingDelayMilli >= LEVEL_3_PING) {
            return getSignalResId(3);
        }else if (mPingDelayMilli >= LEVEL_4_PING) {
            return getSignalResId(4);
        } else {
            return getSignalResId(5);
        }
    }

    public static int getSignalResId(int level) {
        switch (level){
            case 1:
                return R.drawable.img_signal01;
            case 2:
                return R.drawable.img_signal02;
            case 3:
                return R.drawable.img_signal03;
            case 4:
                return R.drawable.img_signal04;
            case 5:
                return R.drawable.img_signal05;
            case 0:
            default:
                return R.drawable.img_signal_none;
        }
    }
}
