package winterfell.flash.vpn.network;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.polestar.ad.AdLog;

import winterfell.flash.vpn.core.ShadowsocksPingManager;
import winterfell.flash.vpn.reward.network.datamodels.RegionServers;
import winterfell.flash.vpn.reward.network.datamodels.VpnServer;
import winterfell.flash.vpn.reward.network.responses.ServersResponse;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.core.LocalVpnService;
import winterfell.flash.vpn.ui.HomeActivity;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

import static com.polestar.task.database.DatabaseFileImpl.readOnelineFromFile;
import static com.polestar.task.database.DatabaseFileImpl.writeOneLineToFile;

public class VPNServerIntermediaManager {
    /**
     * 2019-03-10
     * 这个类是服务器数据和UI数据的桥梁
     * <p>
     * 1，会将从服务器拉下来的裸数据存盘
     * 2，会将各个vpnserver在客户端的性能数据存下来
     * 3，会将服务器的数据和本地的服务器数据进行地区和ip比对；进行增删操作；
     * 4，会根据性能数据进行地区内部的服务器排序，从而提供地区的最优服务器
     * 5，可以接受本地的性能数据并且存储
     */
    private static final String TAG = "VPNServerInter";

    private static final String DIR = "vservs";
    private static final String sRawFileName = DIR + "/serverfile.txt";
    private static final String sInterFileName = DIR + "/intermedia.txt";

    private static VPNServerIntermediaManager sInstance = null;

    private ServersResponse mRawServersResponse; //从服务器来的
    private ServersResponse mInterServers; //包含了性能数据的

    private Context mContext;
    private Gson mGson;
    private RegionServersComparator mRegionComparator = new RegionServersComparator();
    private VpnServerComparator mServerComparator = new VpnServerComparator();
    private GetBestRegionServersComparator mBestServerComparator = new GetBestRegionServersComparator();

    private Handler mainHandler;
    private Handler workHandler;

    private VPNServerIntermediaManager(Context context) {
        mContext = context;
        mGson = new Gson();

        loadRawServerInfo();
        loadInterServerInfo();
        checkSelected();

        mainHandler = new Handler(Looper.getMainLooper());
        HandlerThread thread = new HandlerThread("sync");
        thread.start();
        workHandler = new Handler(thread.getLooper());
    }

    public static VPNServerIntermediaManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VPNServerIntermediaManager(context);
        }
        return sInstance;
    }

    private void createDirIfNotExist(String dirName) {
        File file = new File(mContext.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    private ServersResponse loadServerInfo(String fileName) {
        createDirIfNotExist(DIR);
        File file = new File(mContext.getFilesDir(), fileName);
        ServersResponse ret = null;
        synchronized (this) {
            String info = readOnelineFromFile(file.getAbsolutePath());
            if (info == null) {
                return new ServersResponse();
            } else {
                try {
                    ret = mGson.fromJson(info, ServersResponse.class);
                } catch (JsonSyntaxException jse) {
                    return new ServersResponse();
                }
            }
        }
        if (ret != null && ret.mVpnServers != null) {
            AdLog.i(TAG, "Loaded " + ret.mVpnServers.size() + " regions from disk");
        } else {
            AdLog.i(TAG, "Loaded 0 regions from disk");
        }
        return ret;
    }

    private void loadRawServerInfo() {
        mRawServersResponse = loadServerInfo(sRawFileName);
    }

    private void loadInterServerInfo() {
        mInterServers = loadServerInfo(sInterFileName);
    }


    // called with lock held outside
    private boolean storeServerInfoSynced(ServersResponse serversResponse, String fileName) {
        File file = new File(mContext.getFilesDir(), fileName);
        return writeOneLineToFile(file.getAbsolutePath(), mGson.toJson(serversResponse));
    }

    private boolean storeRawServerInfoSynced() {
        return storeServerInfoSynced(mRawServersResponse, sRawFileName);
    }

    private boolean storeInterServerInfoSynced() {
        return storeServerInfoSynced(mInterServers, sInterFileName);
    }

    private boolean storeInterServerInfo() {
        synchronized (this) {
            return storeInterServerInfoSynced();
        }
    }

    public boolean updateRawServerInfo(ServersResponse serversResponse) {
        if (serversResponse == null) {
            MLogs.e("incoming raw server response is null, this should not happend, do nothing");
            return false;
        }

        boolean ret = true;
        synchronized (this) {
            mRawServersResponse = serversResponse;
            ret &= storeRawServerInfoSynced();

            syncWithInterServerInfoSynced();
            ret &= sortInterServersAndSave();

            checkSelected();
        }
        return ret;
    }

    private void checkSelected() {
        synchronized (this) {
            int currentId = PreferenceUtils.getPreferServer();
            if (currentId == VpnServer.SERVER_ID_AUTO) {
                return;
            }
            VpnServer vpnServer = getServerInfo(currentId);
            if (vpnServer == null) {
                //之前选择的已经被删除了
                PreferenceUtils.setPreferServer(VpnServer.SERVER_ID_AUTO);
            }
        }
    }

    private void syncWithInterServerInfoSynced() {
        if (mInterServers == null) {
            mInterServers = new ServersResponse();
        }
        if (mInterServers.mVpnServers == null) {
            mInterServers.mVpnServers = new ArrayList<>();
        }
        if (mRawServersResponse.mVpnServers == null) {
            //没有的话，给个空的
            mRawServersResponse.mVpnServers = new ArrayList<>();
        }

        addOrRemoveRegions(mInterServers.mVpnServers, mRawServersResponse.mVpnServers);

        for (RegionServers inter : mInterServers.mVpnServers) {
            RegionServers hit = null;
            for (RegionServers incoming : mRawServersResponse.mVpnServers) {
                if (inter.getId() == incoming.getId()) {
                    hit = incoming;
                    break;
                }
            }

            if (hit == null) {
                MLogs.e("This shouldn't happen; Cannot find region " + inter.mRegion.mGeo +
                        inter.mRegion.mCity + " after merge regions");
            } else {
                addOrRemoveServers(inter.mServers, hit.mServers);
            }
        }
    }

    //incoming and exist 不能为 null
    private void addOrRemoveRegions(ArrayList<RegionServers> exists, ArrayList<RegionServers> incomings) {
        for (RegionServers incoming : incomings) {
            boolean hit = false;
            for (RegionServers exist : exists) {
                if (incoming.hasValidId() && incoming.getId() == exist.getId()) {
                    hit = true; //找到了
                }
            }
            if (!hit) {
                exists.add(incoming);
            }
        }

        Iterator<RegionServers> existIt = exists.iterator();
        while (existIt.hasNext()) {
            RegionServers exist = existIt.next();
            boolean hit = false;

            for (RegionServers incoming : incomings) {
                if (incoming.hasValidId() && incoming.getId() == exist.getId()) {
                    hit = true;
                }
            }

            if (!hit) {
                existIt.remove();
            }
        }
    }

    // 输入的不能为null
    private void addOrRemoveServers(ArrayList<VpnServer> exists, ArrayList<VpnServer> incomings) {
        for (VpnServer incoming : incomings) {
            boolean hit = false;
            for (VpnServer exist : exists) {
                if (incoming.mPublicIp != null && incoming.mPublicIp.equals(exist.mPublicIp)) {
                    hit = true; //找到了
                }
            }
            if (!hit) {
                exists.add(incoming);
            }
        }

        Iterator<VpnServer> existIt = exists.iterator();
        while (existIt.hasNext()) {
            VpnServer exist = (VpnServer) existIt.next();
            boolean hit = false;

            for (VpnServer incoming : incomings) {
                if (incoming.mPublicIp != null && incoming.mPublicIp.equals(exist.mPublicIp)) {
                    hit = true;
                }
            }

            if (!hit) {
                existIt.remove();
            }
        }
    }

    public boolean sortInterServersAndSave() {
        synchronized (this) {
            Collections.sort(mInterServers.mVpnServers, mRegionComparator);
            for (RegionServers regionServers : mInterServers.mVpnServers) {
                Collections.sort(regionServers.mServers, mServerComparator);
            }
            return storeInterServerInfoSynced();
        }
    }


    public static class RegionServersComparator implements Comparator<RegionServers> {
        @Override
        public int compare(RegionServers regionServers, RegionServers t1) {
            int countryRet = regionServers.mRegion.mGeo.compareToIgnoreCase(t1.mRegion.mGeo);
            return countryRet == 0 ? regionServers.mRegion.mCity.compareToIgnoreCase(t1.mRegion.mCity) : countryRet;
        }
    }

    public static class VpnServerComparator implements Comparator<VpnServer> {

        @Override
        public int compare(VpnServer vpnServer, VpnServer t1) {
            //TODO may consider connect delay and speed
            if (vpnServer.mPingDelayMilli < t1.mPingDelayMilli) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static class GetBestRegionServersComparator implements Comparator<RegionServers> {
        @Override
        public int compare(RegionServers regionServers, RegionServers t1) {
            if (regionServers.getFirstServer().mPingDelayMilli < t1.getFirstServer().mPingDelayMilli) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public boolean hasValidServers() {
        synchronized (this) {
            return mInterServers.mVpnServers.size() > 0;
        }
    }

    private void dump(ArrayList<RegionServers> servers) {
        for (RegionServers regionServers : servers) {
            regionServers.dump();
        }
    }

    //可能是null哦
    public VpnServer getBestServer() {
        synchronized (this) {
            ArrayList<RegionServers> toSort = new ArrayList<>(mInterServers.mVpnServers);
            Collections.sort(toSort, mBestServerComparator);

            if (toSort.size() > 0) {
                return toSort.get(0).getFirstServer();
            } else {
                return null;
            }
        }
    }

    public VpnServer getServerInfo(int id) {
        if (id == VpnServer.SERVER_ID_AUTO) {
            return getBestServer();
        }
        synchronized (this) {
            for (RegionServers server : mInterServers.mVpnServers) {
                if (server.getId() == id) {
                    return server.getFirstServer();
                }
            }
        }
        return null;
    }

    public ArrayList<RegionServers> getDupInterRegionServers() {
        synchronized (this) {
            return new ArrayList<>(mInterServers.mVpnServers);
        }
    }

    private void updatePing(ServersResponse servers){
        ArrayList<Thread> pingThreads = new ArrayList<>();
        ArrayList<RegionServers> dup = null;
        synchronized (this) {
            dup = new ArrayList<>(servers.mVpnServers);
        }

        for (RegionServers rs: dup) {
            String ip = rs.getFirstServer().mPublicIp;
            PingNetEntity pingNetEntity=new PingNetEntity(ip,
                    3,5,new StringBuffer());
            PingThread pingThread = new PingThread(pingNetEntity, rs.getFirstServer());
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
        private VpnServer mServerInfo;
        public PingThread(PingNetEntity pingNetEntity, VpnServer serverInfo) {
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
                    mServerInfo.mPingDelayMilli = Integer.valueOf(m.group());
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            MLogs.d("Thread " + Thread.currentThread().getId() + " Ping " + mPingNetEntity.getIp() + " res: " + mServerInfo.mPingDelayMilli);
        }
    }

    public interface OnUpdatePingListener{
        void onPingUpdated(boolean res);
    }

    private void updatePingUseTunnel(ServersResponse servers){
        ArrayList<RegionServers> dup = null;
        synchronized (this) {
            dup = new ArrayList<>(servers.mVpnServers);
        }

        int count = 0;
        for (RegionServers rs: dup) {
            VpnServer vpnServer = rs.getFirstServer();
            if (!vpnServer.toSSPingConfig(FlashApp.getApp()).isEmpty()) {
                //万一服务器没配pingport
                count++;
            }
        }

        CountDownLatch waiter = new CountDownLatch(count);

        for (RegionServers rs: dup) {
            VpnServer vpnServer = rs.getFirstServer();
            if (!vpnServer.toSSPingConfig(FlashApp.getApp()).isEmpty()) {
                PingTunnelThread pingThread = new PingTunnelThread(vpnServer, waiter);
                pingThread.start();
            }
        }
        try {
            waiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

        }
        MLogs.d("Ping finished");
    }

    private class PingTunnelThread extends Thread {
        private VpnServer mServerInfo;
        private CountDownLatch mWaiter;
        private ShadowsocksPingManager mPingManager;
        public PingTunnelThread(VpnServer serverInfo, CountDownLatch waiter) {
            mServerInfo = serverInfo;
            mWaiter = waiter;
            mPingManager = ShadowsocksPingManager.getInstance();
        }

        @Override
        public void run() {
            mPingManager.ping(mServerInfo.toSSPingConfig(FlashApp.getApp()), new ShadowsocksPingManager.ShadowsocksPingListenser() {

                @Override
                public void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli) {
                    MLogs.i("VPNServerIntermediaManager-- ShadowsocksPingManager-- pingsucceeded " + pingTimeInMilli + " " + CommonUtils.getIpString(serverAddress));
                    //updateStateOnMainThread(STATE_CHECK_PORT_SUCCEED, "");
                    mServerInfo.mPingDelayMilli = (mServerInfo.mPingDelayMilli + (int)pingTimeInMilli)/2;
                    mWaiter.countDown();
                }

                @Override
                public void onPingFailed(InetSocketAddress socketAddress) {
                    MLogs.i("VPNServerIntermediaManager-- ShadowsocksPingManager-- pingfailed " + this);
                    //updateStateOnMainThread(STATE_CHECK_PORT_FAILED, "");
                    mServerInfo.mPingDelayMilli = VpnServer.NO_PING;
                    mWaiter.countDown();
                }
            }, RemoteConfig.getLong("config_check_port_timeout"));
        }
    }

    private static long lastPingUpdateTime = 0;
    private static final long PING_UPDATE_INTERVAL_MS = 100*1000;

    public void asyncUpdatePing(final VPNServerIntermediaManager.OnUpdatePingListener listener, boolean force) {
//        long now = System.currentTimeMillis();
//        if (!force && (now - lastPingUpdateTime) > PING_UPDATE_INTERVAL_MS) {
//            if (listener != null) {
//                mainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        listener.onPingUpdated(true);
//                    }
//                });
//
//            }
//        }


        long now = System.currentTimeMillis();
        if (force || (now - lastPingUpdateTime) > PING_UPDATE_INTERVAL_MS) {
            workHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtils.isNetConnected(FlashApp.getApp())) {
                        lastPingUpdateTime = System.currentTimeMillis();
                        if (RemoteConfig.getLong("use_tunnel_ping") == 0) {
                            if (LocalVpnService.IsRunning) {
                                MLogs.i("asyncUpdatePing do nothing since localVpnService is running");
                                //updatePing(activeServers);
                            } else {
                                updatePing(mInterServers);
                            }
                        } else {
                            updatePingUseTunnel(mInterServers);
                        }

                        sortInterServersAndSave();

                        if (listener != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onPingUpdated(true);
                                }
                            });

                        }
                    } else {
                        if (listener != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onPingUpdated(false);
                                }
                            });
                        }
                    }
                }
            });
        } else {
            MLogs.i("No update ping too frequent");
        }
    }
}
