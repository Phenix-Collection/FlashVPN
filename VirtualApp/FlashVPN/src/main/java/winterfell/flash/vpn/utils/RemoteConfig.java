package winterfell.flash.vpn.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdControlInfo;

import java.util.ArrayList;
import java.util.List;

import winterfell.flash.vpn.BuildConfig;
import winterfell.flash.vpn.R;

/**
 * Created by guojia on 2016/12/17.
 */

public class RemoteConfig {

    private static FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static String TAG = "RemoteConfig";

    public static void init () {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        int cacheTime = BuildConfig.DEBUG ? 0 : 2*60*60;
        mFirebaseRemoteConfig.setDefaults(R.xml.default_remote_config);
        mFirebaseRemoteConfig.fetch(cacheTime).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MLogs.logBug(TAG, "Fetch Succeeded");
                mFirebaseRemoteConfig.activateFetched();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                MLogs.logBug(TAG, "Fetch failed" + exception);
            }
        });
        mFirebaseRemoteConfig.activateFetched();
    }

    public static boolean getBoolean(String key) {
        return mFirebaseRemoteConfig.getBoolean(key);
    }

    public static long getLong(String key) {
        return mFirebaseRemoteConfig.getLong(key);
    }

    public static String getString(String key) {
        return mFirebaseRemoteConfig.getString(key);
    }

    public static AdControlInfo getAdControlInfo(String placement) {
        String config = getString(placement);
        if (TextUtils.isEmpty(config)) {
            return  null;
        }
        String[] arr = config.split(":");
        if (arr == null || arr.length != 3) {
            MLogs.d("Wrong config : " + config);
            return  null;
        }
        try {
            int random = Integer.valueOf(arr[0]);
            int coldDown = Integer.valueOf(arr[1]);
            if (arr[2].equalsIgnoreCase("wifi")) {
                return new AdControlInfo(AdControlInfo.NETWORK_WIFI_ONLY, random, coldDown);
            } else {
                return new AdControlInfo(AdControlInfo.NETWORK_BOTH,random,coldDown);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    //fb:adfdf:-1;ab:sdff:-2;
    public static List<AdConfig> getAdConfigList(String placement) {
        String config = getString(placement);
        if (TextUtils.isEmpty(config)) {
            return new ArrayList<>();
        }
        MLogs.d(TAG, "placement: " + placement);
        MLogs.d(TAG, "config: " + config);
        List<AdConfig> configList = new ArrayList<>();
        String[] sources = config.split(";");
        for (String s: sources) {
            String[] configs = s.split(":");
            if (configs == null || configs.length < 2) {
                MLogs.e("Wrong config: " + s);
                continue;
            }
            int cachTime = 0;
            if (configs.length == 3) {
                try {
                    cachTime = Integer.valueOf(configs[2]);
                } catch (Exception e){
                    MLogs.e("Wrong config: " + config);
                }
            }
            if (cachTime <= 0) {
                //default cache time
                cachTime = 15*60; //15min
            }
            int bannerType = -1;
            if (configs.length == 4) {
                try {
                    bannerType = Integer.valueOf(configs[3]);
                } catch (Exception e){
                    MLogs.e("Wrong config: " + config);
                }
            }
            if (bannerType == -1){
                configList.add(new AdConfig(configs[0], configs[1], cachTime));
            } else {
                configList.add(new AdConfig(configs[0], configs[1], cachTime, bannerType));
            }
        }
        return configList;
    }
}
