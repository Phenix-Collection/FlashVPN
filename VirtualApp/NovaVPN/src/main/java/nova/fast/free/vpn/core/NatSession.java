package nova.fast.free.vpn.core;

import nova.fast.free.vpn.tcpip.CommonMethods;
import nova.fast.free.vpn.utils.CommonUtils;
import nova.fast.free.vpn.utils.MLogs;

public class NatSession {
    public int RemoteIP;
    public short RemotePort;
    public String RemoteHost;
    public int BytesSent;
    public int PacketSent;
    public long LastNanoTime;

    public void dump(int port) {
        MLogs.d( "NatSession-- " + (port & 0xFFFF) + "-->" +RemoteHost + "==" + CommonMethods.ipIntToString(RemoteIP) + ":" + (RemotePort & 0xFFFF)
                    + " packets:" + PacketSent + " bytes:" + BytesSent + " lastTime:" + LastNanoTime);
    }
}
