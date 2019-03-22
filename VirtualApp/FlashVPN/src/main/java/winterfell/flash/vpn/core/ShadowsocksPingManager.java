package winterfell.flash.vpn.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import winterfell.flash.vpn.tunnel.Config;
import winterfell.flash.vpn.tunnel.Tunnel;
import winterfell.flash.vpn.tunnel.shadowsocks.ShadowsocksPingTunnel;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.MLogs;

import static winterfell.flash.vpn.core.TunnelFactory.getShadowSocksCheckPortConfig;

public class ShadowsocksPingManager implements Runnable {
    public static abstract class ShadowsocksPingListenser {
        private boolean alreadyCalled = false;

        private void onPingSucceededInternal(InetSocketAddress serverAddress, long pingTimeInMilli) {
            if (!alreadyCalled) {
                alreadyCalled = true;
                onPingSucceeded(serverAddress, pingTimeInMilli);
            }
        }

        private void onPingFailedInternal(InetSocketAddress socketAddress) {
            if (!alreadyCalled) {
                alreadyCalled = true;
                onPingFailed(socketAddress);
            }
        }

        public abstract void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli);
        public abstract void onPingFailed(InetSocketAddress socketAddress);
    }

    private class RegisterRequest {
        SelectableChannel channel;
        int ops;
        Object object;
        public RegisterRequest(SelectableChannel c, int op, Object o) {
            channel = c;
            ops = op;
            object = o;
        }
    }

    private Pipe mPipe;
    private boolean mIsRunning = false;
    private Selector mSelector;
    private ConcurrentLinkedQueue<RegisterRequest> mPendingRegisters = new ConcurrentLinkedQueue<>();
    private Thread mPingThread;

    private static ShadowsocksPingManager sInstance = null;

    public static ShadowsocksPingManager getInstance() {
        if (sInstance == null) {
            sInstance = new ShadowsocksPingManager();
            sInstance.start();
            CommonUtils.sleepHelper(1000);
        }
        return sInstance;
    }

    private ShadowsocksPingManager() {
    }

    public void start() {
        if (!this.mIsRunning) {
            try {
                mPipe = Pipe.open();
                mSelector = Selector.open();
                mPipe.source().configureBlocking(false);
                mPipe.source().register(mSelector, SelectionKey.OP_READ);
            } catch (IOException e) {
                MLogs.e("ShadowsocksPingManager-- Failed to start ShadowsocksPingManager");
                stop();
                return;
            }

            mPingThread = new Thread(this);
            mPingThread.setName("ShadowsocksPingManager-- ShadowsocksPingManagerThread");
            mPingThread.start();
            this.mIsRunning = true;
        }
    }

    public void stop() {
        this.mIsRunning = false;
        if (mSelector != null) {
            try {
                mSelector.close();
                mSelector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mPipe != null && mPipe.source() != null) {
            try {
                mPipe.source().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mPipe != null && mPipe.sink() != null) {
            try {
                mPipe.sink().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPipe = null;
    }

    //private long mTimeoutThresholder = 30000; //30秒超时

    public boolean checkPort(InetSocketAddress pingTarget, final ShadowsocksPingListenser listener, final long timeout) {
        start();

        Config config = null;
        try {
            config = getShadowSocksCheckPortConfig(pingTarget);
        } catch (Exception e) {
            MLogs.e("Failed to get config for " + pingTarget.toString());
            return false;
        }
        //schedule timeout
        ScheduledExecutorService scheduler
                = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new PingTimeoutRunnable(config.ServerAddress, listener);
        final Future f = scheduler.schedule(task, timeout, TimeUnit.MILLISECONDS);
        scheduler.shutdown();


        ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser tunnelListenser = new ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser() {
            @Override
            public void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli) {
                if (listener != null) {
                    f.cancel(true);
                    listener.onPingSucceededInternal(serverAddress, pingTimeInMilli);
                }
            }

            @Override
            public void onPingFailed(InetSocketAddress socketAddress) {
                if (listener != null) {
                    f.cancel(true);
                    listener.onPingFailedInternal(socketAddress);
                }
            }
        };

        Tunnel tunnel = null;
        try {
            tunnel = TunnelFactory.createShadowSocksCheckPortTunnelByConfig(pingTarget, null, this, tunnelListenser);
            tunnel.connect(pingTarget);
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.e("ShadowsocksPingManager-- connect or create tunnel failed " + e.toString());
            if (listener != null && tunnel != null) {
                f.cancel(true);
                listener.onPingFailedInternal(tunnel.getServerEP());
            }
        }
        return true;
    }

    public boolean ping(String configUrl,
                        final ShadowsocksPingListenser listener, final long timeout) {
        start();
        final InetSocketAddress pingTarget = InetSocketAddress.createUnresolved("whoer.net", 443);

        Config config = null;
        try {
            MLogs.i("ShadowsocksPingManager-- configURL is " + configUrl);
            config = ProxyConfig.getPingTunnelConfig(configUrl);
        } catch (Exception e) {
            MLogs.e("Failed to get config for " + configUrl);
            return false;
        }
        //schedule timeout
        ScheduledExecutorService scheduler
                = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new PingTimeoutRunnable(config.ServerAddress, listener);
        final Future f = scheduler.schedule(task, timeout, TimeUnit.MILLISECONDS);
        scheduler.shutdown();


        ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser tunnelListenser = new ShadowsocksPingTunnel.ShadowsocksPingTunnelListenser() {
            @Override
            public void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli) {
                if (listener != null) {
                    f.cancel(true);
                    listener.onPingSucceededInternal(serverAddress, pingTimeInMilli);
                }
            }

            @Override
            public void onPingFailed(InetSocketAddress socketAddress) {
                if (listener != null) {
                    f.cancel(true);
                    listener.onPingFailedInternal(socketAddress);
                }
            }
        };

        Tunnel tunnel = null;
        try {
            tunnel = TunnelFactory.createShadowSocksPingTunnel(configUrl, null, this, tunnelListenser);
            tunnel.connect(pingTarget);
        } catch (Exception e) {
            e.printStackTrace();
            MLogs.e("ShadowsocksPingManager-- connect or create tunnel failed " + e.toString());
            if (listener != null && tunnel != null) {
                f.cancel(true);
                listener.onPingFailedInternal(tunnel.getServerEP());
            }
        }
        return true;
    }

    private class PingTimeoutRunnable implements Runnable {
        private InetSocketAddress mAddress;
        private ShadowsocksPingListenser mListerner;
        public PingTimeoutRunnable(InetSocketAddress address, ShadowsocksPingListenser listenser) {
            mAddress = address;
            mListerner = listenser;
        }

        @Override
        public void run() {
            MLogs.e("ShadowsocksPingManager-- ping timeout " + mAddress.toString());
            mListerner.onPingFailedInternal(mAddress);
        }
    }


    private void registerInternal(SelectableChannel channel, int ops, Object object) throws ClosedChannelException {
        channel.register(mSelector, ops, object);
    }

    public void register(SelectableChannel channel, int ops, Object object) throws IOException {
        mPendingRegisters.add(new RegisterRequest(channel, ops, object));
        MLogs.i("ShadowsocksPingManager-- after add pendingsize " + mPendingRegisters.size());
        ByteBuffer junk = ByteBuffer.allocateDirect(1);
        while (mPipe.sink().write(junk) == 0) ;
    }

    @Override
    public void run() {
        while (mIsRunning) {
            try {
                //MLogs.i("ShadowsocksPingManager-- before select");
                int selectret = mSelector.select();
                //MLogs.i("ShadowsocksPingManager-- after select " + selectret);
                if (selectret <= 0) continue;

                Iterator<SelectionKey> keyIterator = mSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    if (selectionKey.isValid()) {
                        if (selectionKey.isReadable() && selectionKey.channel() == mPipe.source()) {
                            //有人请求加一个register
                            ByteBuffer junk = ByteBuffer.allocateDirect(1);
                            while (mPipe.source().read(junk) > 0) {
                                //有多少个1 就有多少个pending的
                                try {
                                    RegisterRequest registerRequest = mPendingRegisters.remove(); //可能会crash
                                    MLogs.i("ShadowsocksPingManager-- after REMOVE pendingsize " + mPendingRegisters.size());
                                    registerInternal(registerRequest.channel, registerRequest.ops, registerRequest.object);
                                } catch (Exception e) {
                                    MLogs.e("ShadowsocksPingManager-- Get pending requests failed " + e.toString());
                                }
                                junk.clear();
                            }
                        } else {
                            if (selectionKey.isConnectable()) {
                                //MLogs.i("ShadowsocksPingManager-- onConnectable");
                                ((Tunnel) selectionKey.attachment()).onConnectable();
                                //selectionKey.cancel();
                            }
                        }
                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                MLogs.e("ShadowsocksPingManager-- Error while running ping manager" + e.toString());
                break;
            }
        }
        stop();
    }
}
