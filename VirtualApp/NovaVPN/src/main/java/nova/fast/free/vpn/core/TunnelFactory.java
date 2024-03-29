package nova.fast.free.vpn.core;

import nova.fast.free.vpn.tunnel.Config;
import nova.fast.free.vpn.tunnel.RawTunnel;
import nova.fast.free.vpn.tunnel.Tunnel;
import nova.fast.free.vpn.tunnel.httpconnect.HttpConnectConfig;
import nova.fast.free.vpn.tunnel.httpconnect.HttpConnectTunnel;
import nova.fast.free.vpn.tunnel.shadowsocks.ShadowsocksConfig;
import nova.fast.free.vpn.tunnel.shadowsocks.ShadowsocksTunnel;
import nova.fast.free.vpn.utils.MLogs;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfig.Instance.getDefaultTunnelConfig(destAddress);
            if (config instanceof HttpConnectConfig) {
                MLogs.d("create HttpConnectTunnel");
                return new HttpConnectTunnel((HttpConnectConfig) config, selector);
            } else if (config instanceof ShadowsocksConfig) {
                MLogs.d("create ShadowsocksTunnel");
                return new ShadowsocksTunnel((ShadowsocksConfig) config, selector);
            }
            throw new Exception("The config is unknow.");
        } else {
            MLogs.d("create RawTunnel");
            return new RawTunnel(destAddress, selector);
        }
    }

}
