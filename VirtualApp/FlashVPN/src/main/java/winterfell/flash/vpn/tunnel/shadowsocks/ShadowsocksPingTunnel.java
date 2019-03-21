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

    private int mWriteRetryCount;
    private boolean mDoHandShake;
    private ICrypt m_Encryptor;
    private ShadowsocksConfig m_Config;

    public ShadowsocksPingTunnel(ShadowsocksConfig config, Selector selector, ShadowsocksPingManager pingManager, ShadowsocksPingTunnelListenser listenser) throws Exception {
        super(config, selector);
        mListener = listenser;
        mShadowsocksPingManger = pingManager;
        mDoHandShake = true;
        m_Config = config;
        m_Encryptor = CryptFactory.get(m_Config.EncryptMethod, m_Config.Password);
    }

    //override Tunnel的write，这里的write会一直重试，直到3秒超时
    @Override
    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        int bytesSent;
        mWriteRetryCount = 0;
        while (buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(buffer);
            MLogs.d("ShadowsocksPingTunnel-- wrote bytes " + bytesSent + " " + buffer.toString() + " " + buffer.limit()  + m_InnerChannel.toString() + " " + getDestAddressString());
            if (bytesSent == 0) {
                mWriteRetryCount++;
                if (mWriteRetryCount < 10) {
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {

                    }
                } else {
                    break;//不能再发送了，终止循环
                }
            }
        }

        if (buffer.hasRemaining()) {//数据没有发送完毕
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    //这里的onConnected；抄自ShadowsocksTunnel,是shadowssocks的握手过程；加了onPingSucceeded 和 fail
    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        if (!mDoHandShake) {
            mFinishConnectTime = Calendar.getInstance().getTimeInMillis();
            MLogs.i("ShadowsocksPingTunnel-- onConnected succeeded " + getDestAddressString() + " " + mFinishConnectTime);
            if (mListener != null) {
                mListener.onPingSucceeded(m_ServerEP, mFinishConnectTime - mStartConnectTime);
            }
        } else {
            buffer.clear();
            // https://shadowsocks.org/en/spec/protocol.html

            buffer.put((byte) 0x03);//domain
            byte[] domainBytes = m_DestAddress.getHostName().getBytes();
            buffer.put((byte) domainBytes.length);//domain length;
            buffer.put(domainBytes);
            buffer.putShort((short) m_DestAddress.getPort());
            buffer.flip();
            byte[] _header = new byte[buffer.limit()];
            buffer.get(_header);

            buffer.clear();
            buffer.put(m_Encryptor.encrypt(_header));
            buffer.flip();

            if (write(buffer, true)) {
                MLogs.d("ShadowsocksPingTunnel-- handshake write succeed");
                mFinishConnectTime = Calendar.getInstance().getTimeInMillis();
                if (mListener != null) {
                    mListener.onPingSucceeded(m_ServerEP, mFinishConnectTime - mStartConnectTime);
                }
            } else {
                MLogs.d("ShadowsocksPingTunnel-- handshake write fail");
                if (mListener != null) {
                    mListener.onPingFailed(m_ServerEP);
                }
            }
        }
        dispose();
    }

    //这里的tunnel的selector是依赖于ShadowsocksPingManager的；因此继承重写了
    @Override
    public void connect(InetSocketAddress destAddress) {
        mStartConnectTime = Calendar.getInstance().getTimeInMillis();
        MLogs.i("ShadowsocksPingTunnel-- connect " + getDestAddressString() + " " + mStartConnectTime + this);
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
        } catch (Throwable e) {
            MLogs.e("ShadowsocksPingTunnel-- connect exception " + e.toString());
            if (mListener != null) {
                mListener.onPingFailed(m_ServerEP);
            }
        }
    }
}
