package nova.fast.free.vpn.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import nova.fast.free.vpn.R;
import nova.fast.free.vpn.tunnel.Config;
import nova.fast.free.vpn.tunnel.httpconnect.HttpConnectConfig;
import nova.fast.free.vpn.tunnel.shadowsocks.ShadowsocksConfig;
import nova.fast.free.vpn.utils.CommonUtils;

public class ServerInfo implements Comparable<ServerInfo>{
    //priority  range [0,9], smaller with higher priority
    public static int PRIORITY_DEFAULT = 5;
    public static int NO_PING = 3000; //1
    public static int PING_TIMEOUT = 5000; //0
    public static int LEVEL_0_PING = PING_TIMEOUT; //1
    public static int LEVEL_1_PING = NO_PING; //1
    public static int LEVEL_2_PING = 1000;
    public static int LEVEL_3_PING = 500;
    public static int LEVEL_4_PING = 200;
    public int priority = PRIORITY_DEFAULT;
    public String url ;
    public String country;
    public String city;
    public int id;
    public boolean isVIP;
    public int pingDelayMs = NO_PING;
    public Config config;

    public static final int SERVER_ID_AUTO = -1;

    private ServerInfo(){}

    public static ServerInfo fromJSON(JSONObject jsonObject){
        ServerInfo si = new ServerInfo();
        si.id = jsonObject.optInt("id", -1);
        si.isVIP = jsonObject.optBoolean("vip", false);
        si.pingDelayMs = jsonObject.optInt("ping", LEVEL_3_PING);
        si.country = jsonObject.optString("geo");
        si.city = jsonObject.optString("city");
        si.priority = jsonObject.optInt("pri", PRIORITY_DEFAULT);
        si.url = jsonObject.optString("url");
        if(!TextUtils.isEmpty(si.url)) {
            try {
                if (si.url.startsWith("ss://")) {
                    si.config = ShadowsocksConfig.parse(si.url);
                } else if (si.url.startsWith("http://")) {
                    si.config = HttpConnectConfig.parse(si.url);
                }
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return si;
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(url) || id == -1 || config == null){
            return false;
        }
        return true;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("ping", pingDelayMs);
            jsonObject.put("vip", isVIP);
            jsonObject.put("url", url);
            jsonObject.put("geo", country);
            jsonObject.put("city", city);
            jsonObject.put("pri",priority);
        }catch (JSONException ex){
            ex.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public String toString() {
        return this.toJSON().toString();
    }

    @Override
    public int compareTo(@NonNull ServerInfo o) {
        if (pingDelayMs != o.pingDelayMs) {
            return pingDelayMs < o.pingDelayMs ? -1:1;
        }

        return priority < o.priority ? -1:1;
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
        return getFlagResId(country);
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
        if (pingDelayMs >= LEVEL_0_PING) {
            return getSignalResId(0);
        } else if (pingDelayMs >= LEVEL_1_PING) {
            return getSignalResId(1);
        } else if (pingDelayMs >= LEVEL_2_PING) {
            return getSignalResId(2);
        }else if (pingDelayMs >= LEVEL_3_PING) {
            return getSignalResId(3);
        }else if (pingDelayMs >= LEVEL_4_PING) {
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
