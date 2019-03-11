package winterfell.flash.vpn.tunnel.shadowsocks;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Calendar;

import winterfell.flash.vpn.core.LocalVpnService;
import winterfell.flash.vpn.core.ShadowsocksPingManager;
import winterfell.flash.vpn.utils.MLogs;

public class ShadowsocksPingTunnel extends ShadowsocksTunnel {
    private long mStartConnectTime;
    private long mFinishConnectTime;
    private ShadowsocksPingTunnelListenser mListener;
    private ShadowsocksPingManager mShadowsocksPingManger;

    public interface ShadowsocksPingTunnelListenser {
        void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli);
        void onPingFailed(InetSocketAddress socketAddress);
    }

    public ShadowsocksPingTunnel(ShadowsocksConfig config, Selector selector, ShadowsocksPingManager pingManager, ShadowsocksPingTunnelListenser listenser) throws Exception {
        super(config, selector);
        mListener = listenser;
        mShadowsocksPingManger = pingManager;
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
//        MLogs.i("ShadowsocksPingTunnel-- onConnected succeeded " + getDestAddressString());

        mFinishConnectTime = Calendar.getInstance().getTimeInMillis();
        if (mListener != null) {
            mListener.onPingSucceeded(m_ServerEP, mFinishConnectTime-mStartConnectTime);
        }
        dispose();
    }

    //这里的tunnel的selector是依赖于ShadowsocksPingManager的；因此继承重写了
    @Override
    public void connect(InetSocketAddress destAddress) {
        mStartConnectTime = Calendar.getInstance().getTimeInMillis();
        try {
//            MLogs.d("Tunnel-- connect " + destAddress.toString() + " proxy is " + m_ServerEP.toString()
//                    + " " + m_InnerChannel.toString() + " Threadid " + Thread.currentThread().getId());
            if (LocalVpnService.Instance != null && LocalVpnService.IsRunning) {
                if (!LocalVpnService.Instance.protect(m_InnerChannel.socket())) {
                    throw new Exception("Tunnel-- VPN protect socket failed." + m_InnerChannel.toString());
                }
            }
            m_DestAddress = destAddress;
            mShadowsocksPingManger.register(m_InnerChannel, SelectionKey.OP_CONNECT, this);
//            MLogs.d("Tunnel-- before connect " + m_ServerEP  + m_InnerChannel.toString() + " " + destAddress.toString() );

            m_InnerChannel.connect(m_ServerEP);//连接目标
        } catch (Exception e) {
            MLogs.e("ShadowsocksPingTunnel-- connect exception " + e.toString());
            if (mListener != null) {
                mListener.onPingFailed(m_ServerEP);
            }
        }
    }
}
