package winterfell.flash.vpn.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;

import winterfell.flash.vpn.reward.network.datamodels.VpnServer;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.R;
import winterfell.flash.vpn.core.ProxyConfig.IPAddress;
import winterfell.flash.vpn.dns.DnsPacket;
import winterfell.flash.vpn.tcpip.CommonMethods;
import winterfell.flash.vpn.tcpip.IPHeader;
import winterfell.flash.vpn.tcpip.TCPHeader;
import winterfell.flash.vpn.tcpip.UDPHeader;
import winterfell.flash.vpn.tunnel.TunnelStatisticManager;
import winterfell.flash.vpn.ui.HomeActivity;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalVpnService extends VpnService implements Runnable {

    public static LocalVpnService Instance;
    public static String ProxyUrl;
    public static boolean IsRunning = false;

    private static int ID;
    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> m_OnStatusChangedListeners = new ConcurrentHashMap<onStatusChangedListener, Object>();

    private Thread m_VPNThread;
    private ParcelFileDescriptor m_VPNInterface;
    private TcpProxyServer m_TcpProxyServer;
    private DnsProxy m_DnsProxy;
    private FileOutputStream m_VPNOutputStream;

    private byte[] m_Packet;
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private Handler m_Handler;
    private long m_SentBytes;
    private long m_ReceivedBytes;
    private int NOTIFY_ID = 10001;
    private long lastSpeedCalTime;
    private long lastDownBytes;
    private long lastUpBytes;
    private final int MSG_UPDATE_NOTIFICATION = 100;

    private float mAvgDownloadSpeed;
    private float mAvgUploadSpeed;
    private float mMaxDownloadSpeed;
    private float mMaxUploadSpeed;

    private final String CONF_VIP_SPEED_BOOST = "conf_vip_speed_boost";
    private final String CONF_NORMAL_SPEED_BOOST = "conf_normal_speed_boost";

    public LocalVpnService() {
        ID++;
        m_Handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_UPDATE_NOTIFICATION:
                        updateNotification();
                            sendMessageDelayed(obtainMessage(MSG_UPDATE_NOTIFICATION), 5000);
                        break;
                }
            }
        };
        m_Packet = new byte[20000];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
        Instance = this;

        MLogs.d("LocalVpnService-- New VPNService(%d)\n"+ ID);
    }

    @Override
    public void onCreate() {
        MLogs.d("LocalVpnService-- (%s) created.\n" + ID);
        // Start a new session by creating a new thread.
        m_VPNThread = new Thread(this, "VPNServiceThread");
        m_VPNThread.start();
        m_Handler.sendMessageDelayed(m_Handler.obtainMessage(MSG_UPDATE_NOTIFICATION), 5000);
        super.onCreate();
    }

    private void resetSpeeds() {
        mAvgDownloadSpeed = 0;
        mAvgUploadSpeed = 0;
        mMaxDownloadSpeed = 0;
        mMaxUploadSpeed = 0;
    }

    private void recalculateSpeed(float currentDownloadSpeed, float currentUploadSpeed) {
        if (currentDownloadSpeed > mMaxDownloadSpeed) {
            mMaxDownloadSpeed = currentDownloadSpeed;
        }
        if (currentUploadSpeed > mMaxUploadSpeed) {
            mMaxUploadSpeed = currentUploadSpeed;
        }
        mAvgUploadSpeed = (mAvgUploadSpeed + currentUploadSpeed)/2;
        mAvgDownloadSpeed = (mAvgDownloadSpeed + currentDownloadSpeed)/2;

        MLogs.i("mAvgDownloadSpeed:" + mAvgDownloadSpeed
                + " mAvgUploadSpeed:" + mAvgUploadSpeed
                + " mMaxDownloadSpeed:" + mMaxDownloadSpeed
                + " mMaxUploadSpeed:" + mMaxUploadSpeed);
    }

    private void updateNotification() {
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
//                if (Build.VERSION.SDK_INT >= 26) {
                Intent intent = new Intent(Instance, HomeActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(Instance, 0, intent, 0);
                Notification notification;
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String channel_id = "_id_service_";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        notificationManager.getNotificationChannel(channel_id) == null) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Flash VPN", importance);
//                notificationChannel.enableVibration(false);
                    notificationChannel.enableLights(false);
//                notificationChannel.setVibrationPattern(new long[]{0});
                    notificationChannel.setSound(null, null);
                    notificationChannel.setDescription("Flash VPN information");
                    notificationChannel.setShowBadge(false);
                    //notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(notificationChannel);
                }
                NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(Instance, channel_id);
                String title;
                if (IsRunning) {
                    if (FlashUser.isVIP() || FlashUser.getInstance().getFreePremiumSeconds() > 0) {
                        title =getString(R.string.notification_connected);
                    } else {
                        title = getString(R.string.notification_connected_not_premium);
                    }
                } else {
                    title = getString(R.string.notification_to_connect);
                }
                float[] speed = getNetworkSpeed();
                DecimalFormat format = new DecimalFormat("0.0");
                String downSpeed = format.format((speed[0] > 1000)? speed[0]/1000:speed[0]);
                downSpeed += (speed[0] > 1000) ? "KB/s":"Byte/s";
                String upSpeed = format.format((speed[1] > 1000)? speed[1]/1000:speed[1]);
                upSpeed += (speed[1] > 1000) ? "KB/s":"Byte/s";

                mBuilder.setContentTitle(title)
                        .setContentText("Down " + downSpeed + " Up " + upSpeed)
                        .setContentIntent(pendingIntent);
                VpnServer vpnServer = ProxyConfig.Instance.getCurrentVpnServer();
                if (vpnServer != null) {
                    mBuilder.setSmallIcon(ProxyConfig.Instance.getCurrentVpnServer().getFlagResId());
                }

                notification = mBuilder.build();
                notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
                try {
                    notificationManager.notify(NOTIFY_ID, notification);
                    startForeground(NOTIFY_ID, notification);

                }catch (Exception ex) {
                    ex.printStackTrace();
                }

                //NatSessionManager.dump();
                TunnelStatisticManager.getInstance().dump();
            }
//            }
        });
    }

    private float[] getRealNetworkSpeed() {
        return getNetworkSpeedInternal(false);
    }

    private float[] getNetworkSpeed() {
        return getNetworkSpeedInternal(true);
    }

    private float[] getNetworkSpeedInternal(boolean tryBoost) {
        float boost = 1.0f;
        if (IsRunning) {
            boost = (tryBoost && (FlashUser.getInstance().isVIP() || FlashUser.getInstance().getFreePremiumSeconds() > 0))
                    ? ((float) RemoteConfig.getLong(CONF_VIP_SPEED_BOOST)) / 100
                    : ((float) RemoteConfig.getLong(CONF_NORMAL_SPEED_BOOST)) / 100;
        }
        long current = System.currentTimeMillis();
        long sent = TrafficStats.getTotalTxBytes();
        long received = TrafficStats.getTotalRxBytes();

        float realDownloadSpeed = ((float)(received - lastDownBytes))*1000/(current - lastSpeedCalTime);
        float realUploadSpeed = ((float)(sent - lastUpBytes))*1000/(current - lastSpeedCalTime);;
        recalculateSpeed(realDownloadSpeed, realUploadSpeed);

        float[] result = new float[2];
        result[0] = ((float)(received - lastDownBytes))*boost*1000/(current - lastSpeedCalTime);
        result[1] = ((float)(sent - lastUpBytes))*boost*1000/(current - lastSpeedCalTime);
        lastSpeedCalTime = current;
        lastUpBytes = sent;
        lastDownBytes = received;
        return result;
    }

    @Override
    public int onStartCommand(Intent cmd, int flags, int startId) {
        MLogs.d("LocalVpnService-- onStartCommand");
        updateNotification();
        IsRunning = true;
        return super.onStartCommand(cmd, flags, startId);
    }

    @Override
    public void onRevoke() {
        // 2019-0311 当从系统的vpndialog去disconnect时，这个onRevoke会被调用到
        // onRevoke -> onDestroy
        super.onRevoke();
        MLogs.d("LocalVpnService-- onRevoke");
    }

    public interface onStatusChangedListener {
        public void onStatusChanged(String status, Boolean isRunning, float avgDownloadSpeed, float avgUploadSpeed,
                                    float maxDownloadSpeed, float maxUploadSpeed);

        public void onLogReceived(String logString);
    }

    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.remove(listener);
        }
    }

    private void onStatusChanged(final String status, final boolean isRunning,
                                final float avgDownloadSpeed, final float avgUploadSpeed,
                                 final float maxDownloadSpeed, final float maxUploadSpeed) {
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning, avgDownloadSpeed, avgUploadSpeed,
                                                                        maxDownloadSpeed, maxUploadSpeed);
                }
            }
        });
    }

    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
            }
        });
    }

    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            MLogs.i("LocalVpnService-- sendUDPPacket ");
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getAppInstallID() {
        SharedPreferences preferences = getSharedPreferences("SmartProxy", MODE_PRIVATE);
        String appInstallID = preferences.getString("AppInstallID", null);
        if (appInstallID == null || appInstallID.isEmpty()) {
            appInstallID = UUID.randomUUID().toString();
            Editor editor = preferences.edit();
            editor.putString("AppInstallID", appInstallID);
            editor.apply();
        }
        return appInstallID;
    }

    String getVersionName() {
        try {
            PackageManager packageManager = getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String version = packInfo.versionName;
            return version;
        } catch (Exception e) {
            return "0.0";
        }
    }

//    private void retrieveBestProxy() throws Exception {
//        ProxyConfig.Instance.m_ProxyList.clear();
//        int id = PreferenceUtils.getPreferServer();
//        String url;
//        if (id == ServerInfo.SERVER_ID_AUTO) {
//            url = VPNServerManager.getInstance(LocalVpnService.this).getBestServer().url;
//        } else {
//            url = VPNServerManager.getInstance(LocalVpnService.this).getServerInfo(id).url;
//        }
//        MLogs.d("LocalVpnService-- Will use url " + url);
//        ProxyUrl = url;
//        ProxyConfig.Instance.addProxyToList(url);
//        MLogs.d("LocalVpnService-- Proxy is:  " + ProxyConfig.Instance.getDefaultProxy());
//        ProxyConfig.Instance.dump();
//    }

    @Override
    public synchronized void run() {
        try {
            MLogs.d("LocalVpnService-- VPNService(%s) work thread is runing...\n"+ ID);

            ProxyConfig.AppInstallID = getAppInstallID();//获取安装ID
            ProxyConfig.AppVersion = getVersionName();//获取版本号
            MLogs.d("LocalVpnService-- AppInstallID: %s\n", ProxyConfig.AppInstallID);
            MLogs.d("LocalVpnService-- Android version: %s", Build.VERSION.RELEASE);
            MLogs.d("LocalVpnService-- App version: %s", ProxyConfig.AppVersion);


            ChinaIpMaskManager.loadFromFile(getResources().openRawResource(R.raw.ipmask));//加载中国的IP段，用于IP分流。
            waitUntilPreapred();//检查是否准备完毕。

            m_TcpProxyServer = new TcpProxyServer(0);
            m_TcpProxyServer.start();
            MLogs.d("LocalVpnService-- LocalTcpServer started.");

            m_DnsProxy = new DnsProxy();
            m_DnsProxy.start();
            MLogs.d("LocalVpnService-- LocalDnsProxy started.");

            while (true) {
                if (IsRunning) {
                    m_Handler.sendMessageDelayed(m_Handler.obtainMessage(MSG_UPDATE_NOTIFICATION), 5000);
                    //加载配置文件
                    String welcomeInfoString = ProxyConfig.Instance.getWelcomeInfo();
                    if (welcomeInfoString != null && !welcomeInfoString.isEmpty()) {
                        MLogs.d("LocalVpnService-- %s", ProxyConfig.Instance.getWelcomeInfo());
                    }
                    MLogs.d("LocalVpnService-- Global mode is " + (ProxyConfig.Instance.globalMode ? "on" : "off"));

                    runVPN();
                } else {
                    Thread.sleep(200);
                }
            }
        } catch (InterruptedException e) {
            MLogs.e("LocalVpnService-- Interrupted " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.e("LocalVpnService-- Fatal error" + e.toString());
        } finally {
            MLogs.d("LocalVpnService-- App terminated.");
            dispose();
        }
    }

    private void runVPN() throws Exception {
        this.m_VPNInterface = establishVPN();
        this.m_VPNOutputStream = new FileOutputStream(m_VPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(m_VPNInterface.getFileDescriptor());
        int size = 0;
        long last = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        while (size != -1 && IsRunning) {
            while ((size = in.read(m_Packet)) > 0 && IsRunning) {
                if (m_DnsProxy.Stopped || m_TcpProxyServer.Stopped) {
                    in.close();
                    MLogs.e("LocalVpnService-- LocalServer Stopped");
                    throw new Exception("LocalServer stopped.");
                }
                onIPPacketReceived(m_IPHeader, size);
                long deduct = 0;
                long current = System.currentTimeMillis();
                if (current - last > 1000) {
                    deduct = (current - last) / 1000;
                    FlashUser.getInstance().costFreePremiumSec(deduct);
                }
                if (deduct > 0) {
                    last = current - (current - last - deduct*1000);
                }
            }
            Thread.sleep(20);
            long deduct = 0;
            long current = System.currentTimeMillis();
            if (current - last > 1000) {
                deduct = (current - last) / 1000;
                FlashUser.getInstance().costFreePremiumSec(deduct);
            }
            if (deduct > 0) {
                last = current - (current - last - deduct*1000);
            }
            if (current - start > 10000) {
                PreferenceUtils.addConnectedTimeSec((current - start) / 1000);
                start = current;
            }
        }
        in.close();
        disconnectVPN();
        PreferenceUtils.addReceiveBytes(m_ReceivedBytes);
        PreferenceUtils.addSentBytes(m_SentBytes);
        updateNotification();
    }

    public static String ipIntToString(int ip) {
        try {
            return InetAddress.getByAddress(BigInteger.valueOf(ip).toByteArray()).toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    /**
     *
     * 2019-03-02
     * 凡是从TcpProxyServer写过来的，都是得转发给原始app
     * 凡是从原始app写过来的，都是得转发给TcpProxyServer
     * 判断数据从哪儿来是通过srcPort
     * 转发的方法是通过改dstPort
     *
     * 原始app的数据进来后，把其真实的dstIp dstPort保存在NAT表，索引是srcPort
     * 把srcIp改成原始的dstIp; 改dstIp和dstPort，从而转发给TcpProxyServer
     *
     * TcpProxyServer数据进来后，要把srcPort改成外部，srcIp也改成外部；转发给原始app
     */
    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();

//                MLogs.d("LocalVpnService-- ipHeader.getSourceIP " + ipIntToString(ipHeader.getSourceIP())
//                        //+ " LOCALIP " + ipIntToString(LOCAL_IP)
//                        + " tcpHeader.getSourcePort() " + (int)(tcpHeader.getSourcePort() & 0xFFFF)
//                        + " ipHeader.getDestinationIP() " +  ipIntToString(ipHeader.getDestinationIP())
//                        + " tcpHeader.getDestinationPort() " + (int)(tcpHeader.getDestinationPort() & 0xFFFF)
//                + " mTcpProxyServer.port is " + (m_TcpProxyServer.Port&0xFFFF));
                if (ipHeader.getSourceIP() == LOCAL_IP) {
                    if (tcpHeader.getSourcePort() == m_TcpProxyServer.Port) {// 收到本地TCP服务器数据
//                        MLogs.i("LocalVpnService-- received data from ssserver ==>" +
//                                ipIntToString(ipHeader.getDestinationIP()) + ":" + (tcpHeader.getDestinationPort() & 0xFFFF));
                        NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                        if (session != null) {
                            ipHeader.setSourceIP(ipHeader.getDestinationIP());
                            tcpHeader.setSourcePort(session.RemotePort);
                            ipHeader.setDestinationIP(LOCAL_IP);

                            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                            m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                            m_ReceivedBytes += size;
                        } else {
                            MLogs.e("LocalVpnService-- NoSession for port:  " + (tcpHeader.getDestinationPort() &0xFFFF) + ipHeader.toString() + " " + tcpHeader.toString());
                        }
                    } else {

                        // 添加端口映射
                        int portKey = tcpHeader.getSourcePort();
//                        MLogs.i("LocalVpnService--  received data from app " + (portKey&0xFFFF)
//                                + "==>" + ipIntToString(ipHeader.getDestinationIP()) + ":" + (tcpHeader.getDestinationPort() & 0xFFFF));
                        NatSession session = NatSessionManager.getSession(portKey);
                        if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {

                            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                            MLogs.i("LocalVpnService--  creating new natsession for " + " port " + (portKey&0xFFFF) +
                                            " for remote ip "+ CommonMethods.ipIntToString(session.RemoteIP)
                                           );
                        }

                        session.LastNanoTime = System.nanoTime();
                        session.PacketSent++;//注意顺序

                        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                        if (session.PacketSent == 2 && tcpDataSize == 0) {
//                            MLogs.i("LocalVpnService-- Drop ACK 2");
                            return;//丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                        }

                        //分析数据，找到host
                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength();
                            String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);
                            if (host != null) {
//                                MLogs.i("LocalVpnService-- changing remote host of " + (portKey&0xFFFF)
//                                        + " from " + session.RemoteHost
//                                        + " to " + host);
                                session.RemoteHost = host;
                            } else {
                                MLogs.e("LocalVpnService-- No host name found: %s", session.RemoteHost);
                            }
                        }

                        // 转发给本地TCP服务器
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        ipHeader.setDestinationIP(LOCAL_IP);
                        tcpHeader.setDestinationPort(m_TcpProxyServer.Port);

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                        session.BytesSent += tcpDataSize;//注意顺序
                        m_SentBytes += size;
                    }
                }
                break;
            case IPHeader.UDP:
                // 转发DNS数据包：
                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                MLogs.d("LocalVpnService-- UDP ipHeader.getSourceIP " + ipIntToString(ipHeader.getSourceIP()) + " LOCALIP " + ipIntToString(LOCAL_IP));
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {
                    m_DNSBuffer.clear();
                    m_DNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(m_DNSBuffer);
                    MLogs.d("LocalVpnService-- UDP destinationPort is 53");
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        m_DnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }

    private void waitUntilPreapred() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        long start = Calendar.getInstance().getTimeInMillis();
        resetSpeeds();
        TunnelStatisticManager.getInstance().clearEstablishTimes();

        Builder builder = new Builder();
        builder.setMtu(ProxyConfig.Instance.getMTU());
        if (ProxyConfig.IS_DEBUG)
            MLogs.d("LocalVpnService-- setMtu: "  + ProxyConfig.Instance.getMTU());

        IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        if (ProxyConfig.IS_DEBUG)
            MLogs.d("LocalVpnService-- addAddress: " + ipAddress.Address + "\\" + ipAddress.PrefixLength);

        for (ProxyConfig.IPAddress dns : ProxyConfig.Instance.getDnsList()) {
            builder.addDnsServer(dns.Address);
            if (ProxyConfig.IS_DEBUG)
                MLogs.d("LocalVpnService-- addDnsServer:  "+dns.Address);
        }

        if (ProxyConfig.Instance.getRouteList().size() > 0) {
            for (ProxyConfig.IPAddress routeAddress : ProxyConfig.Instance.getRouteList()) {
                builder.addRoute(routeAddress.Address, routeAddress.PrefixLength);
                if (ProxyConfig.IS_DEBUG)
                    MLogs.d("LocalVpnService-- addRoute: "+routeAddress.Address + "/" +routeAddress.PrefixLength);
            }
            builder.addRoute(CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP), 16);

            if (ProxyConfig.IS_DEBUG)
                MLogs.d("LocalVpnService-- addRoute for FAKE_NETWORK: " + CommonMethods.ipIntToString(ProxyConfig.FAKE_NETWORK_IP) + "/" +16);
        } else {
            builder.addRoute("0.0.0.0", 0);
            if (ProxyConfig.IS_DEBUG)
                MLogs.d("LocalVpnService-- addDefaultRoute: 0.0.0.0/0\n");
        }


        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<String>();
        for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
            String value = (String) method.invoke(null, name);
            MLogs.d("LocalVpnService-- getProperty " + name + " is " + value);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
                if (value.replaceAll("\\d", "").length() == 3){//防止IPv6地址导致问题
                    builder.addRoute(value, 32);
                } else {
                    builder.addRoute(value, 128);
                }
                if (ProxyConfig.IS_DEBUG)
                    MLogs.d(name + "= " + value);
            }
        }

//                * By default, all applications are allowed access, except for those denied through this
//                * method.  Denied applications will use networking as if the VPN wasn't running.
//                *
//         * A {@link Builder} may have only a set of allowed applications OR a set of disallowed
//                * ones, but not both. Calling this method after {@link #addAllowedApplication} has already
//         * been called, or vice versa, will throw an {@link UnsupportedOperationException}.
//         *
//         * {@code packageName} must be the canonical name of a currently installed application.
//         * {@link PackageManager.NameNotFoundException} is thrown if there's no such application.
//                *
//         * @throws {@link PackageManager.NameNotFoundException} If the application isn't installed.
//                *
        if (AppProxyManager.isLollipopOrAbove){
            if (AppProxyManager.Instance.proxyAppInfo.size() == 0
                    || PreferenceUtils.isGlobalVPN()){
                MLogs.d("LocalVpnService-- Proxy All Apps");
                //add disallow
                final Set<String> blockedApp = CommonUtils.getBlockedApps();
                for(String s: blockedApp) {
                    PackageManager pm =getPackageManager();
                    PackageInfo pi = null;
                    try{
                        pi = pm.getPackageInfo(s, 0);
                    }catch (Exception ex) {

                    }
                    if (pi != null) {
                        builder.addDisallowedApplication(s);
                    }
                }

            } else {
                builder.addAllowedApplication(getPackageName());//需要把自己加入代理，不然会无法进行网络连接
                for (AppInfo app : AppProxyManager.Instance.proxyAppInfo) {
                    try {
                        builder.addAllowedApplication(app.getPkgName());
                        MLogs.d("LocalVpnService-- Proxy App: " + app.getAppLabel());
                    } catch (Exception e) {
                        e.printStackTrace();
                        MLogs.d("LocalVpnService-- Proxy App Fail: " + app.getAppLabel());
                    }
                }
                //do not need add disallow as it already filtered
            }
        } else {
            MLogs.d("LocalVpnService-- No Pre-App proxy, due to low Android version.");
        }
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);

        builder.setSession(ProxyConfig.Instance.getSessionName());
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        onStatusChanged(ProxyConfig.Instance.getSessionName() + getString(R.string.vpn_connected_status), true,
                            mAvgDownloadSpeed, mAvgUploadSpeed, mMaxDownloadSpeed, mMaxUploadSpeed);

        long establishTime = Calendar.getInstance().getTimeInMillis() - start;
        EventReporter.reportEstablishTime(FlashApp.getApp(), establishTime);
        return pfdDescriptor;
    }

    public void disconnectVPN() {
        try {
            if (m_VPNInterface != null) {
                m_VPNInterface.close();
                m_VPNInterface = null;
            }
        } catch (Exception e) {
            // ignore
        }
        onStatusChanged(ProxyConfig.Instance.getSessionName() + getString(R.string.vpn_disconnected_status), false,
                mAvgDownloadSpeed, mAvgUploadSpeed, mMaxDownloadSpeed, mMaxUploadSpeed);
        this.m_VPNOutputStream = null;
    }

    private synchronized void dispose() {
        // 断开VPN
        disconnectVPN();

        // 停止TcpServer
        if (m_TcpProxyServer != null) {
            m_TcpProxyServer.stop();
            m_TcpProxyServer = null;
            MLogs.d("LocalVpnService-- LocalTcpServer stopped.");
        }

        // 停止DNS解析器
        if (m_DnsProxy != null) {
            m_DnsProxy.stop();
            m_DnsProxy = null;
            MLogs.d("LocalVpnService-- LocalDnsProxy stopped.");
        }

       // stopSelf();
        IsRunning = false;
        //2019-03-11 先去掉，为啥一定要结束进程呢？
        // System.exit(0);
    }

    @Override
    public void onDestroy() {
        //2019-03-11 onDestroy -> interrupt ->java.lang.InterruptedException -> dispose
        MLogs.d("LocalVpnService-- VPNService(%s) destoried.\n"+ ID);
        if (m_VPNThread != null) {
            m_VPNThread.interrupt();
        }
    }
}
