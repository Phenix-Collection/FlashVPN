package winterfell.flash.vpn.core;

import android.util.SparseArray;

import winterfell.flash.vpn.tcpip.CommonMethods;
import winterfell.flash.vpn.utils.MLogs;

public class NatSessionManager {

    static final int MAX_SESSION_COUNT = 60;
    static final long SESSION_TIMEOUT_NS = 60 * 1000000000L;
    static final SparseArray<NatSession> Sessions = new SparseArray<NatSession>();

    public static NatSession getSession(int portKey) {
        NatSession session = Sessions.get(portKey);
        if (session!=null) {
            session.LastNanoTime = System.nanoTime();
        }
        return Sessions.get(portKey);
    }

    public static int getSessionCount() {
        return Sessions.size();
    }

    static void clearExpiredSessions() {
        long now = System.nanoTime();
        for (int i = Sessions.size() - 1; i >= 0; i--) {
            NatSession session = Sessions.valueAt(i);
            if (now - session.LastNanoTime > SESSION_TIMEOUT_NS) {
                Sessions.removeAt(i);
            }
        }
    }

    public static NatSession createSession(int portKey, int remoteIP, short remotePort) {
        if (Sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions();//清理过期的会话。
        }

        NatSession session = new NatSession();
        session.LastNanoTime = System.nanoTime();
        session.RemoteIP = remoteIP;
        session.RemotePort = remotePort;

        if (ProxyConfig.isFakeIP(remoteIP)) {
            MLogs.i("NatSession-- fake ip " + CommonMethods.ipIntToString(remoteIP));
            session.RemoteHost = DnsProxy.reverseLookup(remoteIP);

        }

        if (session.RemoteHost == null) {
            session.RemoteHost = CommonMethods.ipIntToString(remoteIP);
        }
        MLogs.i("NatSession-- remote host is " + session.RemoteHost);
        Sessions.put(portKey, session);
        return session;
    }

    public static void dump() {
        for(int i = 0; i < Sessions.size(); i++) {
            int key = Sessions.keyAt(i);
            // get the object by the key.
            NatSession obj = Sessions.get(key);
            obj.dump(key);
        }
    }
}
