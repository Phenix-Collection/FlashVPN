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

    protected SocketChannel m_InnerChannel;
    private ByteBuffer m_SendRemainBuffer;
    private Selector m_Selector;
    private Tunnel m_BrotherTunnel;
    private boolean m_Disposed;
    protected InetSocketAddress m_ServerEP;
    protected InetSocketAddress m_DestAddress;
    private long mStartConnectTime;

    public InetSocketAddress getServerEP() {
        return m_ServerEP;
    }

    protected String getDestAddressString() {
        if (m_DestAddress != null) {
            return m_DestAddress.toString();
        } else {
            return "NoDestAddress";
        }
    }

    public void dump() {
        if (m_DestAddress != null) {
            MLogs.d("Tunnel-- destAddress " + getDestAddressString());
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
//        MLogs.d("Tunnel-- new " + innerChannel.toString() + " SessionCount " + SessionCount
//             + m_InnerChannel.toString());
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
//        MLogs.d("Tunnel-- connect " + destAddress.toString() + " proxy is " + m_ServerEP.toString()
//                + " " + m_InnerChannel.toString() + " Threadid " + Thread.currentThread().getId());
        if (LocalVpnService.Instance.protect(m_InnerChannel.socket())) {//保护socket不走vpn
            m_DestAddress = destAddress;
//            MLogs.d("Tunnel-- before connect " + m_ServerEP  + m_InnerChannel.toString() + " " + destAddress.toString() + " " + selectionKey.toString());
            mStartConnectTime = Calendar.getInstance().getTimeInMillis();
            m_InnerChannel.connect(m_ServerEP);//连接目标
            SelectionKey selectionKey = m_InnerChannel.register(m_Selector, SelectionKey.OP_CONNECT, this);//注册连接事件
        } else {
            throw new Exception("Tunnel-- VPN protect socket failed." + m_InnerChannel.toString());
        }
    }

    protected void beginReceive() throws Exception {
//        MLogs.d("Tunnel-- beginReceive " + m_InnerChannel.toString()+ " " + getDestAddressString());
        if (m_InnerChannel.isBlocking()) {
            m_InnerChannel.configureBlocking(false);
        }
        m_InnerChannel.register(m_Selector, SelectionKey.OP_READ, this);//注册读事件
    }


    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
//        MLogs.d("Tunnel-- write " + buffer.toString() + " " + buffer.limit()  + m_InnerChannel.toString() + " " + getDestAddressString());
        int bytesSent;
        while (buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(buffer);
//            MLogs.d("Tunnel-- wrote bytes " + bytesSent + " " + buffer.toString() + " " + buffer.limit()  + m_InnerChannel.toString() + " " + getDestAddressString());
            if (bytesSent == 0) {

                /*
                这里很有意思；write有时确实是会返回0，毕竟是async的，这可能是因为server没准备好，或者其他原因；
                当ShadowsocksPingTunnel(没有什么localtunnel)去连ssserver时；onConnnected在ssserver被connect上后会被调到
                然后进行一个握手过程，握手过程中只有write成功了，才会回调onTunnelEstablished
                但是，到了这里write有时确实会返回0，结果导致onTunnelEstablished没被调用。
                但我如果加了下面的sleep 300毫秒不停重试的话，大概1秒以后，就可以把握手数据发送过去了

                奇怪的是什么呢，基本上只有没有brothertunnel的tunnel才会有这种write不过去的情况；
                还有就是，如果不重试，写不过去后，下面的register OP_WRITE 照理说过会儿应该能通知我可以写了，从而我可以把刚刚没写成功的
                再写一遍；但onWritable从来没被调用到。
                */
                /*try {
                    Thread.sleep(300);
                } catch (Exception e) {

                }*/
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
                SelectionKey s = m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
//                if (s == null) {
//                    MLogs.d("Tunnel-- register OP_WRITE FAILED " + buffer.toString() + " " + buffer.limit()  + m_InnerChannel.toString() + " " + getDestAddressString());
//                } else {
//                    MLogs.d("Tunnel-- register OP_WRITE SUUCEED " + s.toString()+ buffer.toString() + " " + buffer.limit()  + m_InnerChannel.toString() + " " + getDestAddressString());
//                }
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    protected void onTunnelEstablished() throws Exception {
        long establishTime = Calendar.getInstance().getTimeInMillis() - mStartConnectTime;
        TunnelStatisticManager.getInstance().setEstablishTime(m_ServerEP, establishTime);
//        MLogs.d("Tunnel-- onTunnelEstablished time is " + establishTime  + m_InnerChannel.toString() + " " + getDestAddressString());
        this.beginReceive();//开始接收数据
        if (m_BrotherTunnel != null) {
            m_BrotherTunnel.beginReceive();//兄弟也开始收数据吧
        }
    }

    @SuppressLint("DefaultLocale")
    public void onConnectable() {
        try {
            //MLogs.d("Tunnel-- onConnectable " + m_InnerChannel.toString() + " " + getDestAddressString() + this);

            if (m_InnerChannel.isConnectionPending()) {
                if (m_InnerChannel.finishConnect()) {//连接成功
                    MLogs.d("Tunnel-- finishConnect succeed " + m_InnerChannel.toString());
                    onConnected(GL_BUFFER);//通知子类TCP已连接，子类可以根据协议实现握手等。
                } else {//连接失败
                    MLogs.d(String.format("Tunnel-- Error: connect to %s failed.", m_ServerEP));
                    this.dispose();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //2019-03-02 当shadowsokcs服务器挂了的时候，会进入这里
            //Tunnel-- Error: connect to 95.179.225.74/95.179.225.74:28388 failed: java.net.ConnectException: Connection refused
            //2019-03-21 当数据不通时，会进入这里
            //Error: connect to /92.38.132.133:26343 failed:java.net.ConnectException: Connection timed outalt7-mtalk.google.com:5228
            MLogs.e("Tunnel-- Error: connect to " + m_ServerEP.toString() + " failed:"  + e.toString() + getDestAddressString() + this);
            TunnelStatisticManager.getInstance().setTunnelBroken(m_ServerEP);
            this.dispose();
        }
    }

    public void onReadable(SelectionKey key) {
//        MLogs.d("Tunnel-- onReadable "  + m_InnerChannel.toString() + " " + getDestAddressString());
        try {
            ByteBuffer buffer = GL_BUFFER;
            buffer.clear();
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                afterReceived(buffer);//先让子类处理，例如解密数据。
                if (isTunnelEstablished() && buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
                    if (m_BrotherTunnel != null) {
                        m_BrotherTunnel.beforeSend(buffer);//发送之前，先让子类处理，例如做加密等。
                        if (!m_BrotherTunnel.write(buffer, true)) {
                            key.cancel();//兄弟吃不消，就取消读取事件。
                            MLogs.e("Tunnel-- " + m_ServerEP.toString() + "can not read more.");
                        }
                    }
                }
//                MLogs.d("Tunnel-- onReadable readed " + bytesRead  + m_InnerChannel.toString() + " " + getDestAddressString());
            } else if (bytesRead < 0) {
                //2019-03-02 当Tun关闭时，这里会被调用；从而关闭tunnel pair
//                MLogs.d("Tunnel-- onReadable readed failed "  + m_InnerChannel.toString() + " " + getDestAddressString());
                this.dispose();//连接已关闭，释放资源。
            }
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.e("Tunnel-- Error: onReadable exception " + e.toString()
                + m_InnerChannel.toString() + getDestAddressString());
            this.dispose();
        }
    }

    public void onWritable(SelectionKey key) {
        try {
//            MLogs.d("Tunnel-- onWritable "  + m_InnerChannel.toString() + " " + getDestAddressString());
            this.beforeSend(m_SendRemainBuffer);//发送之前，先让子类处理，例如做加密等。
            if (this.write(m_SendRemainBuffer, false)) {//如果剩余数据已经发送完毕
                key.cancel();//取消写事件。
                if (isTunnelEstablished()) {
                    if (m_BrotherTunnel != null) {
                        m_BrotherTunnel.beginReceive();//这边数据发送完毕，通知兄弟可以收数据了。
                    }
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
//        MLogs.d("Tunnel-- disposeInternal " + disposeBrother + m_InnerChannel.toString() + " " + getDestAddressString());
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
