package winterfell.flash.vpn.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.polestar.ad.AdLog;
import com.polestar.task.network.datamodels.Region;
import com.polestar.task.network.datamodels.RegionServers;
import com.polestar.task.network.datamodels.VpnServer;
import com.polestar.task.network.responses.ServersResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import winterfell.flash.vpn.utils.MLogs;

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

    private VPNServerIntermediaManager(Context context) {
        mContext = context;
        mGson = new Gson();

        loadRawServerInfo();
        loadInterServerInfo();
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
            ret &= storeInterServerInfoSynced();

            sortInterServers();
        }
        return ret;
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

    public void sortInterServers() {
        synchronized (this) {
            Collections.sort(mInterServers.mVpnServers, mRegionComparator);
            for (RegionServers regionServers : mInterServers.mVpnServers) {
                Collections.sort(regionServers.mServers, mServerComparator);
            }
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
}
