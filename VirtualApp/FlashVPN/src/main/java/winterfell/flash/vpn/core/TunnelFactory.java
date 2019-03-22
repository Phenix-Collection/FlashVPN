package winterfell.flash.vpn.core;

import winterfell.flash.vpn.tunnel.Config;
import winterfell.flash.vpn.tunnel.RawTunnel;
import winterfell.flash.vpn.tunnel.Tunnel;
import winterfell.flash.vpn.tunnel.httpconnect.HttpConnectConfig;
import winterfell.flash.vpn.tunnel.httpconnect.HttpConnectTunnel;
import winterfell.flash.vpn.tunnel.shadowsocks.ShadowsocksConfig;
import winterfell.flash.vpn.tunnel.shadowsocks.ShadowsocksPingTunnel;
import winterfell.flash.vpn.tunnel.shadowsocks.ShadowsocksTunnel;
import winterfell.flash.vpn.utils.MLogs;

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

    public static Tunnel createShadowSocksCheckPortTunnelByConfig(InetSocketAddress destAddress, Selector selector,
                                                                  ShadowsocksPingManager pingManager,
                                                                  ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser listenser) throws Exception {
        MLogs.d("create ShadowsocksPingTunnel");
        return new ShadowsocksPingTunnel(getShadowSocksCheckPortConfig(destAddress), selector, pingManager, listenser);
    }

    public static ShadowsocksConfig getShadowSocksCheckPortConfig(InetSocketAddress destAddress) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfig.Instance.getDefaultTunnelConfig(destAddress);
            if (config instanceof HttpConnectConfig) {
                throw new Exception("The config is not shadowsocks.");
            } else if (config instanceof ShadowsocksConfig) {
                return (ShadowsocksConfig) config;
            }
            throw new Exception("The config is unknow.");
        } else {
            throw new Exception("The config is not shadowsocks.");
        }
    }

    public static Tunnel createShadowSocksPingTunnel(String configUrl, Selector selector,
                                                                  ShadowsocksPingManager pingManager,
                                                                  ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser listenser) throws Exception {
        MLogs.d("create ShadowsocksPingTunnel for ping");
        return new ShadowsocksPingTunnel((ShadowsocksConfig) ProxyConfig.getPingTunnelConfig(configUrl), selector, pingManager, listenser);
    }

}
