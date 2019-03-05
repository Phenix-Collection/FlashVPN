package winterfell.flash.vpn.tunnel;

import android.annotation.SuppressLint;

import winterfell.flash.vpn.core.LocalVpnService;
import winterfell.flash.vpn.utils.MLogs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Calendar;

public abstract class Tunnel {

    final static ByteBuffer GL_BUFFER = ByteBuffer.allocate(20000);
    public static long SessionCount;

    protected abstract void onConnected(ByteBuffer buffer) throws Exception;

    protected abstract boolean isTunnelEstablished();

    protected abstract void beforeSend(ByteBuffer buffer) throws Exception;

    protected abstract void afterReceived(ByteBuffer buffer) throws Exception;

    protected abstract void onDispose();

    private SocketChannel m_InnerChannel;
    private ByteBuffer m_SendRemainBuffer;
    private Selector m_Selector;
    private Tunnel m_BrotherTunnel;
    private boolean m_Disposed;
    private InetSocketAddress m_ServerEP;
    protected InetSocketAddress m_DestAddress;
    private long mStartConnectTime;

    public InetSocketAddress getServerEP() {
        return m_ServerEP;
    }

    public void dump() {
        if (m_DestAddress != null) {
            MLogs.d("Tunnel-- destAddress " + m_DestAddress.toString());
        }
        if (m_ServerEP != null) {
            MLogs.d( "Tunnel-- proxy server " + m_ServerEP.toString());
        }
        //m_BrotherTunnel.dump();
    }

    public Tunnel(SocketChannel innerChannel, Selector selector) {
        this.m_InnerChannel = innerChannel;
        this.m_Selector = selector;
        SessionCount++;
        MLogs.d("Tunnel-- new " + innerChannel.toString() + " SessionCount " + SessionCount
             + m_InnerChannel.toString());
    }

    public Tunnel(InetSocketAddress serverAddress, Selector selector) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.m_InnerChannel = innerChannel;
        this.m_Selector = selector;
        this.m_ServerEP = serverAddress;
        SessionCount++;
        MLogs.d("Tunnel-- new " + serverAddress.toString() + " SessionCount " + SessionCount
                + m_InnerChannel.toString());
    }

    public void setBrotherTunnel(Tunnel brotherTunnel) {
        m_BrotherTunnel = brotherTunnel;
    }

    public void connect(InetSocketAddress destAddress) throws Exception {
        MLogs.d("Tunnel-- connect " + destAddress.toString() + " proxy is " + m_ServerEP.toString()
                + " " + m_InnerChannel.toString());
        if (LocalVpnService.Instance.protect(m_InnerChannel.socket())) {//保护socket不走vpn
            m_DestAddress = destAddress;
            m_InnerChannel.register(m_Selector, SelectionKey.OP_CONNECT, this);//注册连接事件
            MLogs.d("Tunnel-- before connect " + m_ServerEP  + m_InnerChannel.toString());
            mStartConnectTime = Calendar.getInstance().getTimeInMillis();
            m_InnerChannel.connect(m_ServerEP);//连接目标
        } else {
            throw new Exception("Tunnel-- VPN protect socket failed." + m_InnerChannel.toString());
        }
    }

    protected void beginReceive() throws Exception {
//        MLogs.d("Tunnel-- beginReceive " + m_InnerChannel.toString());
        if (m_InnerChannel.isBlocking()) {
            m_InnerChannel.configureBlocking(false);
        }
        m_InnerChannel.register(m_Selector, SelectionKey.OP_READ, this);//注册读事件
    }


    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
//        MLogs.d("Tunnel-- write "  + m_InnerChannel.toString());
        int bytesSent;
        while (buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(buffer);
            if (bytesSent == 0) {
                break;//不能再发送了，终止循环
            }
        }

        if (buffer.hasRemaining()) {//数据没有发送完毕
            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
                //拷贝剩余数据
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = ByteBuffer.allocate(buffer.capacity());
                }
                m_SendRemainBuffer.clear();
                m_SendRemainBuffer.put(buffer);
                m_SendRemainBuffer.flip();
                m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    protected void onTunnelEstablished() throws Exception {
        long establishTime = Calendar.getInstance().getTimeInMillis() - mStartConnectTime;
        TunnelStatisticManager.getInstance().setEstablishTime(m_ServerEP, establishTime);
        MLogs.d("Tunnel-- onTunnelEstablished time is " + establishTime  + m_InnerChannel.toString());
        this.beginReceive();//开始接收数据
        m_BrotherTunnel.beginReceive();//兄弟也开始收数据吧
    }

    @SuppressLint("DefaultLocale")
    public void onConnectable() {
        try {
            MLogs.d("Tunnel-- onConnectable " + m_InnerChannel.toString());
            if (m_InnerChannel.finishConnect()) {//连接成功
                MLogs.d("Tunnel-- finishConnect succeed "  + m_InnerChannel.toString());
                onConnected(GL_BUFFER);//通知子类TCP已连接，子类可以根据协议实现握手等。
            } else {//连接失败
                MLogs.d(String.format("Error: connect to %s failed.", m_ServerEP));
                this.dispose();
            }
        } catch (Exception e) {
            //2019-03-02 当shadowsokcs服务器挂了的时候，会进入这里
            //Tunnel-- Error: connect to 95.179.225.74/95.179.225.74:28388 failed: java.net.ConnectException: Connection refused
            MLogs.e("Tunnel-- Error: connect to " + m_ServerEP.toString() + " failed:"  + e.toString());
            this.dispose();
        }
    }

    public void onReadable(SelectionKey key) {
//        MLogs.d("Tunnel-- onReadable "  + m_InnerChannel.toString());
        try {
            ByteBuffer buffer = GL_BUFFER;
            buffer.clear();
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                afterReceived(buffer);//先让子类处理，例如解密数据。
                if (isTunnelEstablished() && buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
                    m_BrotherTunnel.beforeSend(buffer);//发送之前，先让子类处理，例如做加密等。
                    if (!m_BrotherTunnel.write(buffer, true)) {
                        key.cancel();//兄弟吃不消，就取消读取事件。
                        MLogs.e("Tunnel-- " + m_ServerEP.toString() + "can not read more.");
                    }
                }
            } else if (bytesRead < 0) {
                //2019-03-02 当Tun关闭时，这里会被调用；从而关闭tunnel pair
                this.dispose();//连接已关闭，释放资源。
            }
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.e("Tunnel-- Error: onReadable exception " + e.toString()
                + m_InnerChannel.toString());
            this.dispose();
        }
    }

    public void onWritable(SelectionKey key) {
        try {
//            MLogs.d("Tunnel-- onWritable "  + m_InnerChannel.toString());
            this.beforeSend(m_SendRemainBuffer);//发送之前，先让子类处理，例如做加密等。
            if (this.write(m_SendRemainBuffer, false)) {//如果剩余数据已经发送完毕
                key.cancel();//取消写事件。
                if (isTunnelEstablished()) {
                    m_BrotherTunnel.beginReceive();//这边数据发送完毕，通知兄弟可以收数据了。
                } else {
                    this.beginReceive();//开始接收代理服务器响应数据
                }
            } else {
                MLogs.e("Tunnel-- onWritable Not able to send complete remaining buffer " + m_InnerChannel.toString());
            }
        } catch (Exception e) {
            MLogs.e("Tunnel-- Error: onWritable exception " + e.toString()
            + m_InnerChannel.toString());
            this.dispose();
        }
    }

    public void dispose() {
        //MLogs.d("Tunnel-- dispose "  + m_InnerChannel.toString());
        disposeInternal(true);
    }

    void disposeInternal(boolean disposeBrother) {
        MLogs.d("Tunnel-- disposeInternal " + disposeBrother + m_InnerChannel.toString());
        if (m_Disposed) {
            return;
        } else {
            try {
                m_InnerChannel.close();
            } catch (Exception e) {
            }

            if (m_BrotherTunnel != null && disposeBrother) {
                m_BrotherTunnel.disposeInternal(false);//把兄弟的资源也释放了。
            }

            m_InnerChannel = null;
            m_SendRemainBuffer = null;
            m_Selector = null;
            m_BrotherTunnel = null;
            m_Disposed = true;
            SessionCount--;

            onDispose();
        }
    }
}
