package winterfell.flash.vpn.core;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.tcpip.CommonMethods;
import winterfell.flash.vpn.tunnel.Tunnel;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TcpProxyServer implements Runnable {

    public boolean Stopped = true;
    public short Port;

    Selector m_Selector;
    ServerSocketChannel m_ServerSocketChannel;
    Thread m_ServerThread;

    public TcpProxyServer(int port) throws IOException {
        m_Selector = Selector.open();
        m_ServerSocketChannel = ServerSocketChannel.open();
        m_ServerSocketChannel.configureBlocking(false);
        m_ServerSocketChannel.socket().bind(new InetSocketAddress(port));
        m_ServerSocketChannel.register(m_Selector, SelectionKey.OP_ACCEPT);
        this.Port = (short) m_ServerSocketChannel.socket().getLocalPort();
        MLogs.d("TcpProxyServer-- AsyncTcpServer listen on %d success.", this.Port & 0xFFFF);
    }

    public void start() {
        if (this.Stopped) {
            m_ServerThread = new Thread(this);
            m_ServerThread.setName("TcpProxyServer-- TcpProxyServerThread");
            m_ServerThread.start();
            this.Stopped = false;
        }
    }

    public void stop() {
        this.Stopped = true;
        if (m_Selector != null) {
            try {
                m_Selector.close();
                m_Selector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (m_ServerSocketChannel != null) {
            try {
                m_ServerSocketChannel.close();
                m_ServerSocketChannel = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                //MLogs.d("TcpProxyServer--  whoer before select " + Thread.currentThread().getId());
                m_Selector.select();
                //MLogs.d("TcpProxyServer--  whoer after select, keys size " + m_Selector.selectedKeys().size() + " " + + Thread.currentThread().getId());
                Iterator<SelectionKey> keyIterator = m_Selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isReadable()) {
//                                MLogs.d("TcpProxyServer-- isReadable");
//                                ((Tunnel) key.attachment()).dump();
                                ((Tunnel) key.attachment()).onReadable(key);
                            } else if (key.isWritable()) {
//                                MLogs.d("TcpProxyServer-- isWritable");
//                                ((Tunnel) key.attachment()).dump();
                                ((Tunnel) key.attachment()).onWritable(key);
                            } else if (key.isConnectable()) {
//                                MLogs.d("TcpProxyServer-- isConnectable");
//                                ((Tunnel) key.attachment()).dump();
                                ((Tunnel) key.attachment()).onConnectable();
                            } else if (key.isAcceptable()) {
//                                MLogs.d("TcpProxyServer-- isAcceptable");
                                onAccepted(key);
                            }
                        } catch (Exception e) {
                            MLogs.d(e.toString());
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop();
            System.out.println("TcpProxyServer--  whoer thread exited!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
//            MLogs.i("TcpProxyServer-- getDestAddress for port " + (int)(portKey&0xFFFF)
//                        + " session.RemoteHost " + session.RemoteHost+ " session.RemoteIP " + CommonMethods.ipIntToString(session.RemoteIP)
//                    + " localChannel.socket().getInetAddress() " + localChannel.socket().getInetAddress().toString()
//            + " " + localChannel.toString());
            if (ProxyConfig.Instance.needProxy(session.RemoteHost, session.RemoteIP)) {
                if (ProxyConfig.IS_DEBUG) {
                    MLogs.d("TcpProxyServer-- %d/%d:[PROXY] %s=>%s:%d\n", NatSessionManager.getSessionCount(), Tunnel.SessionCount, session.RemoteHost, CommonMethods.ipIntToString(session.RemoteIP), session.RemotePort & 0xFFFF);
                }
                return InetSocketAddress.createUnresolved(session.RemoteHost, session.RemotePort & 0xFFFF);
            } else {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }

    void onAccepted(SelectionKey key) {
        Tunnel localTunnel = null;
        Tunnel remoteTunnel = null;
        try {
            /**
             * huan 2019-03-02 localChannel是Tun转发过来的其他app的数据通道
             * 其他app向外部发送的数据包被写入Tun，LocalVpnService读取到后，进行nat转换
             * 再次写入Tun，从而被转发到TcpProxyServer；
             *
             * 这里的数据包的dstport应该是TcpProxyServer的port dstIp是LocalIP
             * srcIp是外部，srcPort是内部app的port
             */
            SocketChannel localChannel = m_ServerSocketChannel.accept();
            MLogs.d("TcpProxyServer-- onAccepted " + localChannel.toString());
            localTunnel = TunnelFactory.wrap(localChannel, m_Selector);

            InetSocketAddress destAddress = getDestAddress(localChannel);

            /**
             * 只有localChannel分析出destAddress才会去建立shadowsockstunnel
             * 在tunnelFactory里，如果destAddress是unresolved，才会去建代理socket
             * 不然就是RawTunnel直连
             */
            if (destAddress != null) {
//                MLogs.d("TcpProxyServer-- destAddress " + destAddress.toString());
                remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, m_Selector);
                remoteTunnel.setBrotherTunnel(localTunnel);//关联兄弟
                localTunnel.setBrotherTunnel(remoteTunnel);//关联兄弟
                remoteTunnel.connect(destAddress);//开始连接
                //MLogs.d("TcpProxyServer-- After remoteTunnel connect");
            } else {
//                MLogs.d("TcpProxyServer-- Error: socket(%s:%d) target host is null.", localChannel.socket().getInetAddress().toString(), localChannel.socket().getPort());
                LocalVpnService.Instance.writeLog("Error: socket(%s:%d) target host is null.", localChannel.socket().getInetAddress().toString(), localChannel.socket().getPort());
                localTunnel.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.d("TcpProxyServer-- Error: remote socket create failed: %s", e.toString());
            LocalVpnService.Instance.writeLog("Error: remote socket create failed: %s", e.toString());
            if (remoteTunnel != null) {
                EventReporter.reportTunnelConnectFail(FlashApp.getApp().getResources().getConfiguration().locale.toString(),
                        remoteTunnel.getServerEP().getAddress().toString());
            }
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}
