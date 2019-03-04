package nova.fast.free.vpn.network;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nova.fast.free.vpn.BuildConfig;
import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.R;
import nova.fast.free.vpn.core.LocalVpnService;
import nova.fast.free.vpn.utils.Crypto;
import nova.fast.free.vpn.utils.FileUtils;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;
import nova.fast.free.vpn.utils.RemoteConfig;

public class VPNServerManager {

    public interface FetchServerListener{
        void onServerListFetched(boolean res, final List<ServerInfo> list);
    }
    private ArrayList<ServerInfo> activeServers;
    private static VPNServerManager sInstance;
    private Handler mainHandler;
    private Handler workHandler;

    //String def = "{\"servers\": [{\"id\": 0,\"geo\": \"kr\",\"city\": \"Seoul\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"},{\"id\": 1,\"geo\": \"kr\",\"city\": \"Seoul-2\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://chacha20:ss,2016.11.29@13.124.147.181:8488\"},{\"id\": 2,\"geo\": \"us\",\"city\": \"New York\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://chacha20:ss,2016.11.29@13.124.147.181:8488\"},{\"id\": 3,\"geo\": \"sg\",\"city\": \"Singapore\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://chacha20:ss,2016.11.29@13.124.147.181:8488\"},{\"id\": 4,\"geo\": \"us\",\"city\": \"Los Angles\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://chacha20:ss,2016.11.29@13.124.147.181:8488\"},{\"id\": 5,\"geo\": \"jp\",\"city\": \"Tokyo\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"},{\"id\": 6,\"geo\": \"de\",\"city\": \"Berlin\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"},{\"id\": 7,\"geo\": \"br\",\"city\": \"Sao Paulo\",\"vip\":\"false\",\"pri\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"}]}";
    //String def = "{ \"servers\": [{ \"id\": 0, \"geo\": \"fr\", \"city\": \"Paris\", \"vip\": \"false\", \"pri\": \"100\", \"url\": \"ss://aes-256-cfb:neversleep123@51.15.118.154:28388\" }, { \"id\": 1, \"geo\": \"de\", \"city\": \"Frankfurt\", \"vip\": \"false\", \"pri\": \"100\", \"url\": \"ss://aes-256-cfb:neversleep123@51.15.252.169:28388\" }, { \"id\": 2, \"geo\": \"us\", \"city\": \"SantaClara\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.149.104:28388\" }, { \"id\": 3, \"geo\": \"us\", \"city\": \"Miami\", \"vip\": \"false\", \"pri\": \"100\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.132.133:28388\" }, { \"id\": 4, \"geo\": \"nl\", \"city\": \"Amsterdam\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.184.97:28388\" }, { \"id\": 5, \"geo\": \"jp\", \"city\": \"Tokyo\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.132.133:28388\" }, { \"id\": 6, \"geo\": \"lu\", \"city\": \"Luxembourg\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.223.105.86:28388\" }, { \"id\": 7, \"geo\": \"ru\", \"city\": \"Moscow\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.152.101:28388\" }, { \"id\": 8, \"geo\": \"us\", \"city\": \"Chicago\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@92.38.176.35:28388\" }, { \"id\": 9, \"geo\": \"gb\", \"city\": \"London\", \"vip\": \"false\", \"pri\": \"90\", \"url\": \"ss://aes-256-cfb:neversleep123@95.179.225.74:28388\" }] }";
    private VPNServerManager(Context appContext){
        mainHandler = new Handler(Looper.getMainLooper());
        HandlerThread thread = new HandlerThread("sync");
        thread.start();
        workHandler = new Handler(thread.getLooper());
        activeServers = getActiveServers();
//        String res ;
//        try{
//            String crypted = FileUtils.readRawFile(appContext, R.raw.imgbin);
//            if (crypted != null) {
//                MLogs.d("decrypted：");
//                MLogs.d(Crypto.d(appContext,crypted));
//            } else{
//                MLogs.d("error decrypt!!!");
//            }
//            crypted = Crypto.e(appContext,def);
//            MLogs.d("result is ");
//            MLogs.d(crypted);
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    public ServerInfo getServerInfo(int id) {
        for (ServerInfo si: getActiveServers()) {
            if (si.id == id) {
                return  si;
            }
        }
        return null;
    }

    public static synchronized VPNServerManager getInstance(Context appContext) {
        if(sInstance == null) {
            sInstance = new VPNServerManager(appContext.getApplicationContext());
        }
        return sInstance;
    }

    public final ServerInfo getBestServer() {
        List<ServerInfo> list = getActiveServers();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private ArrayList<ServerInfo> getServerListFromPref() {
        String s = PreferenceUtils.getServerList();
        MLogs.d("prefs: " + s);
        return parseFromString(s);
    }

    private void saveServerListToPref() {
        MLogs.d("save servers");
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        synchronized (this) {
            for (ServerInfo si : activeServers) {
                array.put(si.toJSON());
            }
        }
        try {
            jsonObject.put("servers", array);
            PreferenceUtils.setServerList(jsonObject.toString());
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ArrayList<ServerInfo> parseFromString(String s) {
        ArrayList<ServerInfo> arrayList = new ArrayList<>();
        if (! TextUtils.isEmpty(s)) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("servers");

                for(int i = 0; i < jsonArray.length();i ++) {
                    JSONObject server = jsonArray.getJSONObject(i);
                    ServerInfo serverInfo = ServerInfo.fromJSON(server);
                    if (serverInfo.isValid()) {
                        arrayList.add(serverInfo);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    private ArrayList<ServerInfo> getServerListFromConfig() {
        String config = RemoteConfig.getString("server_list");
        //String config = "{\"servers\": [{\"id\": 0,\"country\": \"kr\",\"priority\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"},{\"id\": 1,\"country\": \"kr\",\"priority\": \"100\",\"url\": \"ss://aes-256-cfb:fuckgfw@13.125.143.115:18388\"}]}";
        MLogs.d("remote config: " + config);

        return parseFromString(config);
    }

    public final ArrayList<ServerInfo> getActiveServers() {
        if(activeServers == null ){
            ArrayList<ServerInfo> list = getServerListFromPref();
            if(list == null || list.size() == 0) {
                list = getServerListFromConfig();
            }
            if(list == null || list.size() == 0) {
                String crypted = FileUtils.readRawFile(NovaApp.getApp(), R.raw.imgbin);
                list = parseFromString(Crypto.d(NovaApp.getApp(),crypted));
            }
            if(list == null || list.size() == 0) {
                MLogs.d("ERROR： No CONFIG");
            }
            synchronized (this) {
                activeServers = list;
            }
        }
        return activeServers;
    }

    private void updatePing(List<ServerInfo> serverInfos){
        ArrayList<ServerInfo> copy = new ArrayList(serverInfos);
        ArrayList<Thread> pingThreads = new ArrayList<>();
        for (ServerInfo si: copy) {
            String ip = si.config.ServerAddress.getAddress().getHostAddress();
            PingNetEntity pingNetEntity=new PingNetEntity(ip,
                    3,5,new StringBuffer());
            PingThread pingThread = new PingThread(pingNetEntity, si);
            pingThreads.add(pingThread);
            pingThread.start();
        }

        for(int i = 0; i < pingThreads.size(); i++) {
            try {
                pingThreads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        MLogs.d("Ping threads all finished");
    }

    private class PingThread extends Thread {
        private PingNetEntity mPingNetEntity;
        private ServerInfo mServerInfo;
        public PingThread(PingNetEntity pingNetEntity, ServerInfo serverInfo) {
            mPingNetEntity = pingNetEntity;
            mServerInfo = serverInfo;
        }

        @Override
        public void run() {
            mPingNetEntity=PingNet.ping(mPingNetEntity);

            if (mPingNetEntity.isResult()) {
                try {
                    Pattern p = Pattern.compile("\\d+");
                    Matcher m = p.matcher(mPingNetEntity.getPingTime());
                    m.find();
                    mServerInfo.pingDelayMs = Integer.valueOf(m.group());
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            MLogs.d("Thread " + Thread.currentThread().getId() + " Ping " + mPingNetEntity.getIp() + " res: " + mServerInfo.pingDelayMs);
        }
    }

    public interface OnUpdatePingListener{
         void onPingUpdated(boolean res, final List<ServerInfo> serverInfos);
    }

    private static long lastPingUpdateTime;
    private static final long PING_UPDATE_INTERVAL_MS = 10*1000;

    public void asyncUpdatePing( final OnUpdatePingListener listener, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - lastPingUpdateTime) > PING_UPDATE_INTERVAL_MS) {
            if (listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPingUpdated(true, activeServers);
                    }
                });

            }
        }

        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtils.isNetConnected(NovaApp.getApp())) {
                    lastPingUpdateTime = System.currentTimeMillis();
                    if (LocalVpnService.IsRunning) {
                        MLogs.i("asyncUpdatePing do nothing since localVpnService is running");
                        //updatePing(activeServers);
                    } else {
                        updatePing(activeServers);
                    }

                    synchronized (this) {
                        Collections.sort(activeServers);
                    }
                    if(listener != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPingUpdated(true, activeServers);
                            }
                        });

                    }
                    saveServerListToPref();
                } else {
                    if(listener != null) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPingUpdated(false, activeServers);
                            }
                        });
                    }
                }
            }
        });
    }

    //5 times in 60s
    public void fetchServerList(final FetchServerListener listener) {
        MLogs.d("fetchServerList " + listener);
            if (!NetworkUtils.isNetConnected(NovaApp.getApp()) && listener != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MLogs.d("No network return directly");
                        listener.onServerListFetched(false, activeServers);
                    }
                });
            }
            final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                    .build();
//            mFirebaseRemoteConfig.setConfigSettings(configSettings);
//            mFirebaseRemoteConfig.setDefaults(R.xml.default_remote_config);
            //Throttle the fetch interval to 20min
            int cacheTime = BuildConfig.DEBUG ? 0 : 20*60;

            //TODO active fetch
            mFirebaseRemoteConfig.fetch(cacheTime).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    MLogs.d("onSuccess");
                    mFirebaseRemoteConfig.activateFetched();
                    ArrayList<ServerInfo> list = getServerListFromConfig();
                    if (list!= null && list.size() > 0) {
                        synchronized (this) {
                            activeServers = list;
                        }
                        if (listener != null) {
                            MLogs.d("get server config from remote success");
                            listener.onServerListFetched(true, activeServers);
                        }
                        return;
                    } else {
                        if (listener != null) {
                            MLogs.d("get server config from remote failed");
                            listener.onServerListFetched(false, activeServers);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    MLogs.d("onFailure");
                    if (listener != null) {
                        MLogs.d("get server config from remote failed");
                        listener.onServerListFetched(false, activeServers);
                    }
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    MLogs.d("onCanceled");
                }
            });
    }
}
