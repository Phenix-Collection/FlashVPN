package winterfell.flash.vpn.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.ads.AdSize;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.network.datamodels.Task;

import winterfell.flash.vpn.reward.AppUser;
import winterfell.flash.vpn.reward.IVpnStatusListener;
import winterfell.flash.vpn.reward.RewardErrorCode;
import winterfell.flash.vpn.reward.TaskExecutor;
import winterfell.flash.vpn.reward.network.VpnApiHelper;
import winterfell.flash.vpn.reward.network.datamodels.VpnRequirement;
import winterfell.flash.vpn.reward.network.datamodels.VpnServer;
import winterfell.flash.vpn.reward.network.responses.ServersResponse;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import winterfell.flash.vpn.AppConstants;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.R;
import winterfell.flash.vpn.core.AppProxyManager;
import winterfell.flash.vpn.core.LocalVpnService;
import winterfell.flash.vpn.core.ProxyConfig;
import winterfell.flash.vpn.core.ShadowsocksPingManager;
import winterfell.flash.vpn.network.VPNServerIntermediaManager;
import winterfell.flash.vpn.tunnel.TunnelStatisticManager;
import winterfell.flash.vpn.ui.widget.RateDialog;
import winterfell.flash.vpn.ui.widget.UpDownDialog;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

/**
 * TODO
 * dowsocksPingManager-- onConnectable
 * 03-11 21:04:39.195 24454 24881 E FlashVPN: Tunnel-- Error: connect to /172.105.232.229:26108 failed:java.net.ConnectException: Connection timed outwhoer.net:443
 * 03-11 21:04:39.195 24454 24881 I FlashVPN: ShadowsocksPingManager-- before select
 * 03-11 21:04:44.184 24454 24881 I FlashVPN: ShadowsocksPingManager-- after select 1
 * 03-11 21:04:44.184 24454 24881 I FlashVPN: ShadowsocksPingManager-- onConnectable
 * 03-11 21:04:44.185 24454 24881 E FlashVPN: Tunnel-- Error: connect to /172.105.232.229:26108 failed:java.net.ConnectException: Connection timed outwhoer.net:443
 * 03-11 21:04:44.185 24454 24881 I FlashVPN: ShadowsocksPingManager-- before select
 * 03-11 21:04:50.463 24454 24881 I FlashVPN: ShadowsocksPingManager-- after select
 *
 * 有些服务器可以ping的非常好，但是connect不上，那么会导致auto时一直选择它；但是一直连不上，草
 * 得解决下
 *
 *
 */

public class HomeActivity extends BaseActivity implements LocalVpnService.onStatusChangedListener,
    TunnelStatisticManager.onSpeedListener
{
    private final static String EXTRA_NEED_UPDATE = "extra_need_update";
    private Handler mainHandler;
    private DrawerLayout drawer;
    private ListView navigationList;
    private ImageView btnCenter;
    private ImageView btnCenterBg;
    private TextView cityText;
    private ImageView geoImage;
    private Animation connectBgAnimation;
    private Animation connectBgMapAnimation;
    private TextView connectTips;
    private TextView connectBtnTxt;
    private boolean connectingFailed;
    private ViewGroup rewardLayout;
    private IAdAdapter rewardAd;
    private long adShowTime = 0;

    private Timer timer;
    private TimerTask timeCountTask;
    private VpnServer mCurrentVpnServer;
    private VpnRequirement mCurrentVpnRequirement;
    private long mGetVpnRequirementTime;
    private int mCheckPortFailedCount;
    private int mState;

    private static int CHECK_PORT_FAILED_THRESHOLDER = 2;

    private static final String SLOT_HOME_BANNER = "slot_home_banner";
    private static final String SLOT_HOME_GIFT_REWARD = "slot_user_center_reward";
    private static final int START_VPN_SERVICE_REQUEST_CODE = 100;
    private static final int SELECT_SERVER_REQUEST_CODE = 101;

    private final static int STATE_ACQUIRING_PORT = 4;
    private final static int STATE_ACQUIRING_CONTROLFLOW_ERR = 5;
    private final static int STATE_IS_RELEASING = 9;
    //private final static int STATE_ACQIRE_FAILED = 5;
    private final static int STATE_CHECKING_PORT = 6;
    private final static int STATE_CHECK_PORT_FAILED = 7;
    private final static int STATE_CHECK_PORT_SUCCEED = 8;

    private final static int STATE_CONNECTED = 0;
    private final static int STATE_DISCONNECTED = 1;
    private final static int STATE_CONNECT_FAILED = 2;
    private final static int STATE_START_CONNECTING = 3;
    private final static int STATE_START_RECONNECT = 10;

    private final static String RATE_FROM_MENU = "rate_from_menu";
    private final static String RATE_FROM_DIALOG = "rate_from_dialog";
    private final static String SLOT_CONNECTED_AD = "slot_connected_ad";
    private final static String CONF_WAIT_AD_INTERVAL = "conf_wait_ad_ms";
    private final static String CONF_RATE_DIALOG_GATE = "rate_vpn_time_sec";

    private boolean needToRequestNewVpnRequirement() {
        if (Calendar.getInstance().getTimeInMillis() - mGetVpnRequirementTime >
                RemoteConfig.getLong("config_request_newport_interval")) {
            return true;
        }
        return false;
    }

    private void setVpnRequirementIntoProxyList(VpnRequirement vpnRequirement) throws Exception {
        ProxyConfig.Instance.m_ProxyList.clear();
        String config = vpnRequirement.toSSConfig(this);
        ProxyConfig.Instance.addProxyToList(config);
    }

    private void acquirePort(VpnServer vpnServer) {
        if (vpnServer == null) {
            MLogs.e("HomeActivity-- no vpnserver found");
            EventReporter.reportNoServer();
            return;
        }
        updateConnectState(STATE_ACQUIRING_PORT, "", false);

        if (mIsReleasing) {
            MLogs.i("HomeActivity-- Still releasing the port, do nothing");
            mIsReleasingCount++;
            if (mIsReleasingCount > 50) {
                //等待isReleasing太久了，不管了
                EventReporter.reportReleasing();
                mIsReleasing = false;
                mIsReleasingCount = 0;
            } else {
                //自动重试
                try {
                    Thread.sleep(200);
                } catch (Exception e) {

                }
                updateStateOnMainThread(STATE_ACQUIRING_CONTROLFLOW_ERR, "");
                return;
            }
        }

        if (needToRequestNewVpnRequirement()) {
            VpnApiHelper.acquireVpnServer(FlashUser.getInstance().getMyId(), vpnServer.mPublicIp,
                    vpnServer.mGeo, vpnServer.mCity, new IVpnStatusListener() {
                        @Override
                        public void onAcquireSucceed(VpnRequirement requirement) {
                            mCurrentVpnRequirement = requirement;
                            mGetVpnRequirementTime = Calendar.getInstance().getTimeInMillis();
                            //需要开始检查服务器状态了
                            try {
                                setVpnRequirementIntoProxyList(mCurrentVpnRequirement);
                            } catch (Exception e) {
                                e.printStackTrace();
                                MLogs.e("HomeActivity-- Failed to add to proxylist " + e.toString());
                                EventReporter.reportFailedToAddProxy();
                                //基本上不应该进入这里的，不然是有问题的
                                return;
                            }
                            //试着去ping
                            updateStateOnMainThread(STATE_CHECKING_PORT, "");
                        }

                        @Override
                        public void onAcquireFailed(String publicIp, ADErrorCode code) {
                            MLogs.e("HomeActivity-- onAcquireFailed " + code.toString());
                            EventReporter.reportGetPortFailed(publicIp + "_" + code.getErrCode());

                            updateStateOnMainThread(STATE_CONNECT_FAILED, code.getErrMsg());
                        }

                        @Override
                        public void onReleaseSucceed(String publicIp) {
                        }

                        @Override
                        public void onReleaseFailed(String publicIp, ADErrorCode code) {
                        }

                        @Override
                        public void onGetAllServers(ServersResponse servers) {
                        }

                        @Override
                        public void onGeneralError(ADErrorCode code) {
                            MLogs.e("HomeActivity-- onGeneralError " + code.toString());
                            EventReporter.reportAderror(code);
                            if (code.getErrCode() == ADErrorCode.FLOW_CONTROL_ERR_CODE) {
                                //自动重试
                                try {
                                    Thread.sleep(200);
                                } catch (Exception e) {

                                }
                                updateStateOnMainThread(STATE_ACQUIRING_CONTROLFLOW_ERR, "");
                            } else {
                                updateStateOnMainThread(STATE_CONNECT_FAILED, code.getErrMsg());
                            }
                        }
                    });
        } else {
            //已经有比较新鲜的vpnrequirement了，直接去check
            updateStateOnMainThread(STATE_CHECKING_PORT, "");
        }
    }

    private boolean mIsReleasing;
    private int mIsReleasingCount;
    private void releasePort(VpnServer vpnServer) {
        if (vpnServer == null) {
            MLogs.e("HomeActivity-- no vpnserver found");
            EventReporter.reportNoServer();
            return;
        }

        mIsReleasing = true;
        mIsReleasingCount = 0;
        VpnApiHelper.releaseVpnServer(FlashUser.getInstance().getMyId(), vpnServer.mPublicIp,
                new IVpnStatusListener() {
                    @Override
                    public void onAcquireSucceed(VpnRequirement requirement) {

                    }

                    @Override
                    public void onAcquireFailed(String publicIp, ADErrorCode code) {
                    }

                    @Override
                    public void onReleaseSucceed(String publicIp) {
                        MLogs.e("HomeActivity-- onReleaseSucceed ");
                        EventReporter.reportRleasePortSucceed(publicIp);
                        mIsReleasing = false;
                    }

                    @Override
                    public void onReleaseFailed(String publicIp, ADErrorCode code) {
                        MLogs.e("HomeActivity-- onReleaseFailed " + code.toString());
                        EventReporter.reportRleasePortFailed(publicIp + "_" + code.getErrCode());
                        mIsReleasing = false;
                    }

                    @Override
                    public void onGetAllServers(ServersResponse servers) {
                    }

                    @Override
                    public void onGeneralError(ADErrorCode code) {
                        MLogs.e("HomeActivity-- onGeneralError " + code.toString());
                        EventReporter.reportAderror(code);
                        mIsReleasing = false;
                    }
                }, true);
    }

    ShadowsocksPingManager mPingManager = ShadowsocksPingManager.getInstance();
    private void checkPort() {
        MLogs.i("HomeActivity-- CHECKPORTCHECKPORTCHECKPORTCHECKPORTCHECKPORTCHECKPORT");
        final InetSocketAddress pingTarget = InetSocketAddress.createUnresolved("whoer.net", 443);

        Thread t = new Thread() {
            @Override
            public void run() {

                mPingManager.checkPort(pingTarget, new ShadowsocksPingManager.ShadowsocksPingListenser() {

                    @Override
                    public void onPingSucceeded(InetSocketAddress serverAddress, long pingTimeInMilli) {
                        MLogs.i("HomeActivity-- ShadowsocksPingManager-- checkPortsucceeded " + pingTimeInMilli);
                        updateStateOnMainThread(STATE_CHECK_PORT_SUCCEED, "");
                    }

                    @Override
                    public void onPingFailed(InetSocketAddress socketAddress) {
                        MLogs.i("HomeActivity-- ShadowsocksPingManager-- checkPortfailed " + this);
                        updateStateOnMainThread(STATE_CHECK_PORT_FAILED, "");
                    }
                }, RemoteConfig.getLong("config_check_port_timeout"));
            }
        };
        t.start();
    }

    private void updateStateOnMainThread(final int state, final String errMsg) {
        updateStateOnMainThreadDelayed(state, errMsg, 0);
    }

    private void updateStateOnMainThreadDelayed(final int state, final String errMsg, long delay) {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateConnectState(state, errMsg, true);
            }
        }, delay);
    }

    private void updateConnectState(int state, String errMsg, boolean doAction) {
        mState = state;
        switch (state) {
            case STATE_ACQUIRING_PORT:
                MLogs.d("HomeActivity-- state STATE_ACQUIRING_PORT");
                btnCenter.setClickable(false);
                connectBtnTxt.setText(R.string.connecting);
                connectTips.setVisibility(View.VISIBLE);
                connectTips.setText(R.string.acquiring_port);
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                break;
            case STATE_ACQUIRING_CONTROLFLOW_ERR:
                MLogs.d("HomeActivity-- state STATE_ACQUIRING_CONTROLFLOW_ERR");
                if (doAction) {
                    acquirePort(mCurrentVpnServer);
                }
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                break;
            case STATE_CHECKING_PORT:
                MLogs.d("HomeActivity-- state STATE_CHECKING_PORT");
                mCheckPortFailedCount = 0;
                btnCenter.setClickable(false);
                connectBtnTxt.setText(R.string.connecting);
                connectTips.setVisibility(View.VISIBLE);
                connectTips.setText(R.string.checking_port);
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                if (doAction) {
                    checkPort();
                }
                break;
            case STATE_CHECK_PORT_SUCCEED:
                MLogs.d("HomeActivity-- state STATE_CHECK_PORT_SUCCEED");
                if (doAction) {
                    mCheckPortFailedCount = 0;
                    startConnect();
                }
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                break;
            case STATE_CHECK_PORT_FAILED:
                MLogs.d("HomeActivity-- state STATE_CHECK_PORT_FAILED");
                if (doAction) {
                    mCheckPortFailedCount++;
                    int retryTimes = (int) RemoteConfig.getLong("config_retry_times");
                    if(PreferenceUtils.getConnectedTimeSec() == 0) {
                        retryTimes = retryTimes*2;
                    }
                    if (mCheckPortFailedCount >= retryTimes) {
                        updateStateOnMainThread(STATE_CONNECT_FAILED, "");
                        return;
                    }
                    //ping不同一次，再来一次，有可能服务器还没准备好
                    checkPort();
                }
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                break;
            case STATE_CONNECTED:
                MLogs.d("HomeActivity-- state STATE_CONNECTED");
                btnCenter.setClickable(true);
                connectTips.setVisibility(View.VISIBLE);
                connectTips.setText(R.string.connecting_tip_success);
                connectBtnTxt.setText(R.string.stop);
                btnCenter.setImageResource(R.drawable.shape_stop_btn);
                btnCenterBg.setImageResource(R.drawable.shape_stop_btn_bg);
                btnCenterBg.setAnimation(connectBgAnimation);
                connectBgAnimation.start();
                break;
            case STATE_DISCONNECTED:
                MLogs.d("HomeActivity-- state STATE_DISCONNECTED");
                btnCenter.setClickable(true);
                mGetVpnRequirementTime = 0;
                if (errMsg != null && !errMsg.isEmpty()) {
                    connectTips.setText(errMsg);
                } else {
                    connectTips.setVisibility(View.INVISIBLE);
                }
                connectBtnTxt.setText(R.string.connect);
                btnCenter.setImageResource(R.drawable.shape_connect_btn);
                btnCenterBg.setImageResource(R.drawable.shape_connect_btn_bg);
                btnCenterBg.setAnimation(connectBgAnimation);
                connectBgAnimation.start();
                break;
            case STATE_START_RECONNECT:
                MLogs.d("HomeActivity-- state STATE_START_RECONNECT");
                btnCenter.setClickable(false);
                connectBtnTxt.setText(R.string.reconnecting);
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventReporter.reportDisConnect(HomeActivity.this, getSIReportValue(mCurrentVpnServer));
                        LocalVpnService.IsRunning = false;
                        releasePort(mCurrentVpnServer);

                        int id = PreferenceUtils.getPreferServer();
                        mCurrentVpnServer = VPNServerIntermediaManager.getInstance(HomeActivity.this).getServerInfo(id);
                        if (!LocalVpnService.IsRunning) {
                            EventReporter.reportConnect(HomeActivity.this, getSIReportValue(mCurrentVpnServer));
                            ProxyConfig.Instance.setCurrentVpnServer(mCurrentVpnServer);
                            acquirePort(mCurrentVpnServer);
                        }
                    }
                }, 1000);
                break;
            case STATE_START_CONNECTING:
                MLogs.d("HomeActivity-- state STATE_START_CONNECTING");
                btnCenter.setClickable(false);
                connectBtnTxt.setText(R.string.connecting);
                btnCenter.setImageResource(R.drawable.shape_connecting_btn );
                btnCenterBg.setImageResource(R.drawable.shape_connecting_btn_bg);
//                Runnable connectingRunnable = new Runnable() {
//                    int step = 0;
//                    @Override
//                    public void run() {
//                        if (connectingFailed) {
//                            updateConnectState(STATE_CONNECT_FAILED, "");
//                            return;
//                        }
//                        if (step <= 2) {
//                            switch (step) {
//                                case 0:
//                                    connectTips.setVisibility(View.VISIBLE);
//                                    connectTips.setText(R.string.connecting_tip1);
//                                    mainHandler.postDelayed(this, 2000);
//                                    break;
//                                case 1:
//                                    connectTips.setText(R.string.connecting_tip2);
//                                    mainHandler.postDelayed(this, 2000);
//                                    break;
//                                case 2:
//                                    updateConnectState(STATE_CONNECTED, "");
//                                    connectTips.setText(R.string.connecting_tip_success);
//                                    break;
//                            }
//                            step ++;
//                        }
//                    }
//                };
//                mainHandler.post(connectingRunnable);
                break;
            case STATE_CONNECT_FAILED:
                btnCenter.setClickable(true);
                connectTips.setVisibility(View.VISIBLE);
                connectBtnTxt.setText(R.string.connect);
                btnCenter.setImageResource(R.drawable.shape_connect_btn);
                btnCenterBg.setImageResource(R.drawable.shape_connect_btn_bg);
                btnCenterBg.setAnimation(connectBgAnimation);
                connectBgAnimation.start();
                connectTips.setText(R.string.retry_tips);
                break;
        }
    }

    public void onVipClick(View view){
        UserCenterActivity.start(this, UserCenterActivity.FROM_HOME_TITLE_ICON);
        EventReporter.generalEvent(this, "home_vip_click");
    }

    public void onRewardClick(View view) {
        if (FlashUser.getInstance().isRewardVideoTaskReady()) {
            new TaskExecutor(this).execute(FlashUser.getInstance().getVideoTask(),
                    new ITaskStatusListener() {
                        @Override
                        public void onTaskSuccess(long taskId, float payment, float balance) {
                            EventReporter.rewardEvent("home_rewarded");
                            RewardErrorCode.toastMessage(HomeActivity.this, RewardErrorCode.TASK_OK, payment);
                            updateRewardLayout();
                        }

                        @Override
                        public void onTaskFail(long taskId, ADErrorCode code) {
                            EventReporter.rewardEvent("home_reward_fail_" + code.getErrMsg());
                            RewardErrorCode.toastMessage(HomeActivity.this, code.getErrCode());
                        }

                        @Override
                        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

                        }

                        @Override
                        public void onGeneralError(ADErrorCode code) {
                            EventReporter.rewardEvent("home_reward_fail_" + code.getErrMsg());
                        }
                    });
        } else {
            UserCenterActivity.start(this, UserCenterActivity.FROM_HOME_GIFT_ICON);
            EventReporter.generalEvent(this, "home_reward_click_user_center");
        }
    }

    private String getSIReportValue(VpnServer si) {
        return si == null ? "null" : si.mPublicIp;
    }

    private void startConnect() {
        Intent intent = LocalVpnService.prepare(HomeActivity.this);
        if (intent == null) {
            startVPNService();
        } else {
            startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
        }
    }

    private void startVPNService() {
        onLogReceived("HomeActivity-- starting...");
        MLogs.d("HomeActivity-- starting vpn service...");
        connectingFailed = false;
        updateConnectState(STATE_START_CONNECTING, "", false);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, LocalVpnService.class));
        } else {
            startService(new Intent(this, LocalVpnService.class));
        }
    }

    @Override
    public void onBrokenSpeed(String ip) {
        MLogs.i("HomeActivity-- onBrokenSpeed " + ip);
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning, float avgDownloadSpeed, float avgUploadSpeed,
                                float maxDownloadSpeed, float maxUploadSpeed) {
        MLogs.i("HomeActivity-- onStatusChanged " + isRunning);
        if (isRunning) {
            EventReporter.reportConnectted(this, getSIReportValue(mCurrentVpnServer));
            connectingFailed = false;

            long delay = 0;

            if (PreferenceUtils.hasShownRateDialog(this)
                    && !FlashUser.getInstance().isVIP()) {
                if (!FuseAdLoader.get(SLOT_CONNECTED_AD, this).hasValidCache()) {
                    delay = RemoteConfig.getLong(CONF_WAIT_AD_INTERVAL);
                }
                FuseAdLoader.get(SLOT_CONNECTED_AD, this).loadAd(this, 2, new IAdLoadListener() {
                    @Override
                    public void onAdLoaded(IAdAdapter ad) {
                        ad.show();
                    }

                    @Override
                    public void onAdClicked(IAdAdapter ad) {

                    }

                    @Override
                    public void onAdClosed(IAdAdapter ad) {

                    }

                    @Override
                    public void onAdListLoaded(List<IAdAdapter> ads) {

                    }

                    @Override
                    public void onError(String error) {

                    }

                    @Override
                    public void onRewarded(IAdAdapter ad) {

                    }
                });
            }
            updateStateOnMainThreadDelayed(STATE_CONNECTED, "", delay);

        } else {
            EventReporter.reportDisConnectted(this, getSIReportValue(mCurrentVpnServer));
            EventReporter.reportSpeed(this, getSIReportValue(mCurrentVpnServer),
                    avgDownloadSpeed, avgUploadSpeed,
                    maxDownloadSpeed, maxUploadSpeed);
            TunnelStatisticManager.getInstance().eventReport();
            connectingFailed = true;
            releasePort(mCurrentVpnServer);
            MLogs.i("avgDownloadSpeed:" + avgDownloadSpeed
                    + " avgUploadSpeed:" + avgUploadSpeed
                    + " maxDownloadSpeed:" + maxDownloadSpeed
                    + " maxUploadSpeed:" + maxUploadSpeed);

            //2019-03-11 如果是localVpnService内部错误导致的，我们是不是应该将其状态设为false，免得他一直跑着呢
            LocalVpnService.IsRunning = false;
            updateStateOnMainThread(STATE_DISCONNECTED, "");
        }
    }

    @Override
    public void onLogReceived(String logString) {

    }


    public void onCountryClick(View view){
        SelectServerActivity.start(this, SELECT_SERVER_REQUEST_CODE);
        EventReporter.generalEvent(this, "home_country_click");
    }

    public boolean onNavigationItemSelected(int position) {
        switch (position) {
            case 0:
                AppProxySettingActivity.start(this);
                break;
            case 1:
                UserCenterActivity.start(this, UserCenterActivity.FROM_HOME_MENU);
                break;
            case 2:
                FaqActivity.start(this);
                break;
            case 3:
                FeedbackActivity.start(this, 0);
                break;
            case 4:
                showRateDialog(RATE_FROM_MENU);
                break;
            case 5:
                CommonUtils.shareWithFriends(this);
                break;
            case 6:
                try {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                } catch (Exception localException1) {
                    localException1.printStackTrace();
                }
                break;
        }
        return true;
    }

    private void updateRewardLayout() {
        rewardLayout.setVisibility(View.VISIBLE);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                long premiumTime = FlashUser.getInstance().getFreePremiumSeconds();
                TextView text = findViewById(R.id.reward_text);
                ImageView giftIcon = rewardLayout.findViewById(R.id.reward_icon);
                if (FlashUser.getInstance().isVIP()) {
                    giftIcon.setImageResource(R.drawable.icon_trophy_award);
                    text.setText(R.string.reward_text_vip);
                } else if (FlashUser.getInstance().isRewardVideoTaskReady()) {
                    if (premiumTime <= 0) {
                        text.setText(R.string.reward_text_no_premium_time_watch_ad);
                    } else {
                        String s = CommonUtils.formatSeconds(premiumTime);
                        text.setText(getString(R.string.reward_text_has_premium_time_watch_ad, s));
                    }
                    giftIcon.setImageResource(R.drawable.icon_reward);
                } else {
                    if (premiumTime <= 0) {
                        text.setText(R.string.reward_text_no_premium_time);
                    } else {
                        String s = CommonUtils.formatSeconds(premiumTime);
                        text.setText(getString(R.string.reward_text_has_premium_time, s));
                    }
                    giftIcon.setImageResource(R.drawable.icon_trophy_award);
                }
            }
        });
    }

    private void initView() {
        setContentView(R.layout.activity_home);
        rewardLayout = findViewById(R.id.reward_layout);
        btnCenter = findViewById(R.id.connect_button);
        btnCenterBg = findViewById(R.id.connect_button_bg);
        connectTips = findViewById(R.id.txt_connect_tips);
        connectBtnTxt = findViewById(R.id.txt_connect_btn);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        navigationList = (ListView) findViewById(R.id.navigation_list);
        navigationList.setAdapter(new HomeNavigationAdapter(this));
        geoImage = findViewById(R.id.img_flag);
        cityText = findViewById(R.id.txt_city);

        connectBgAnimation = AnimationUtils.loadAnimation(this, R.anim.connect_bg);
        connectBgMapAnimation = AnimationUtils.loadAnimation(this, R.anim.connect_bg_map);
        ImageView bgMap = findViewById(R.id.bg_worldmap);
        bgMap.setAnimation(connectBgMapAnimation);
        connectBgMapAnimation.start();

        //        int width = DisplayUtils.getScreenWidth(this);

//        int listWidth = DisplayUtils.px2dip(this, width*2/3);
//        MLogs.d("width set to " + listWidth);
//        navigationList.setLayoutParams(new LinearLayout.LayoutParams(listWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onNavigationItemSelected(i);
                drawer.closeDrawer(GravityCompat.START);
//                int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
//                if (drawer.isDrawerVisible(GravityCompat.START)
//                        && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                }
            }
        });
        View view = findViewById(R.id.content_home);
        setImmerseLayout(view);
        if (AppProxyManager.isLollipopOrAbove){
            new AppProxyManager(this);
            //   textViewProxyApp = (TextView) findViewById(R.id.textViewAppSelectDetail);
        }
        btnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onConnectBtnClick();
            }
        });
    }

    private void onConnectBtnClick() {
        MLogs.d("IsRunning " + LocalVpnService.IsRunning);
        int id = PreferenceUtils.getPreferServer();
        mCurrentVpnServer = VPNServerIntermediaManager.getInstance(HomeActivity.this).getServerInfo(id);

        if (!LocalVpnService.IsRunning) {
            FuseAdLoader.get(SLOT_CONNECTED_AD, this).preloadAd(this);
            EventReporter.reportConnect(HomeActivity.this, getSIReportValue(mCurrentVpnServer));
            ProxyConfig.Instance.setCurrentVpnServer(mCurrentVpnServer);
            acquirePort(mCurrentVpnServer);
        } else {
            EventReporter.reportDisConnect(HomeActivity.this, getSIReportValue(mCurrentVpnServer));
            LocalVpnService.IsRunning = false;
            releasePort(mCurrentVpnServer);
            updateConnectState(STATE_DISCONNECTED, "", false);
        }
    }

    private void reconnect() {
        MLogs.d("reconnect");
        if (!LocalVpnService.IsRunning) {
            //not connected
            return;
        }
        FuseAdLoader.get(SLOT_CONNECTED_AD, this).preloadAd(this);
        updateConnectState(STATE_START_RECONNECT, "", false);
    }

    public static void enter(Activity activity, boolean needUpdate) {
        MLogs.d("Enter home: update: " + needUpdate);
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(EXTRA_NEED_UPDATE, needUpdate);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, -1);
    }
    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        MLogs.d("onActivityResult req : " + requestCode + " result： " + resultCode + " intent: " + intent);
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else{
                updateConnectState(STATE_CONNECT_FAILED, "", false);
            }
            return;
        } else if (requestCode == SELECT_SERVER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                reconnect();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timeCountTask != null) {
            timeCountTask.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int id = PreferenceUtils.getPreferServer();
        VpnServer si = VPNServerIntermediaManager.getInstance(this).getServerInfo(id);
        if (id == VpnServer.SERVER_ID_AUTO) {
            geoImage.setImageResource(R.drawable.flash_black);
            cityText.setText(R.string.select_server_auto);
        } else {
            geoImage.setImageResource(si.getFlagResId());
            cityText.setText(si.mCity);
        }
        updateConnectState(mState, "", false);
        if (FlashUser.getInstance().isRewardVideoTaskReady()) {
            ImageView giftIcon = rewardLayout.findViewById(R.id.reward_icon);
            giftIcon.setImageResource(R.drawable.icon_reward);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(giftIcon, "scaleX", 0.7f, 1.3f, 1.1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(giftIcon, "scaleY", 0.7f, 1.3f, 1.1f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(scaleX).with(scaleY);
            animSet.setInterpolator(new BounceInterpolator());
            animSet.setDuration(800).start();
        }
        updateRewardLayout();

        timeCountTask = new TimerTask() {
            @Override
            public void run() {
                if (LocalVpnService.IsRunning) {
                    updateRewardLayout();
                }
            }
        };
        timer.scheduleAtFixedRate(timeCountTask, 1000, 1000);

        if (!PreferenceUtils.hasShownRateDialog(this)
                && PreferenceUtils.getConnectedTimeSec() > RemoteConfig.getLong(CONF_RATE_DIALOG_GATE)) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showRateDialog(RATE_FROM_DIALOG);
                }
            }, 1000);
        }

        if (!FlashUser.getInstance().isVIP()) {
            long current = System.currentTimeMillis();
            if (current - adShowTime > RemoteConfig.getLong("home_ad_refresh_interval_s") * 1000) {
                loadHomeNativeAds();
            }
            if (!LocalVpnService.IsRunning) {
                FuseAdLoader.get(SLOT_CONNECTED_AD, this).preloadAd(this);
            }
            FlashUser.getInstance().preloadRewardVideoTask();
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!LocalVpnService.IsRunning) {
            mState = STATE_DISCONNECTED;
        } else {
            mState = STATE_CONNECTED;
        }

        initView();

        mainHandler = new Handler();

        boolean needUpdate = getIntent().getBooleanExtra(EXTRA_NEED_UPDATE, false);
        if (needUpdate) {
            MLogs.d("need update");
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUpdateDialog();
                }
            }, 1000);
        }
        VPNServerIntermediaManager.getInstance(HomeActivity.this).asyncUpdatePing(new VPNServerIntermediaManager.OnUpdatePingListener() {
            @Override
            public void onPingUpdated(boolean res) {
                //btnCenter.setClickable(true);
            }
        }, true);

        timer = new Timer();
        loadHomeNativeAds();
        TunnelStatisticManager.getInstance().addOnSpeedListener(this);
        LocalVpnService.addOnStatusChangedListener(this);

        FlashUser.getInstance().listenOnUserUpdate(new AppUser.IUserUpdateListener() {
            @Override
            public void onUserDataUpdated() {
                updateRewardLayout();
            }

            @Override
            public void onVideoTaskAvailable() {
                updateRewardLayout();
            }
        });
    }

    @Override
    protected void onDestroy() {
        TunnelStatisticManager.getInstance().removeOnSpeedListener(this);
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();

    }

    private final static String QUIT_RATE_RANDOM = "quit_rating_random";
    private final static String QUIT_RATE_INTERVAL = "quit_rating_interval";
    private static final String RATE_FROM_QUIT = "quit";

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
            boolean showRate = false;
            if (!PreferenceUtils.isRated()
                    && PreferenceUtils.getConnectedTimeSec() > RemoteConfig.getLong(CONF_RATE_DIALOG_GATE)) {
                MLogs.d("Quit Rate config:" +  RemoteConfig.getLong(QUIT_RATE_INTERVAL)+" , "
                        + RemoteConfig.getLong(QUIT_RATE_RANDOM));
                long interval = RemoteConfig.getLong(QUIT_RATE_INTERVAL) * 60 * 60 * 1000;
                long lastTime = PreferenceUtils.getRateDialogTime(this);
                if (PreferenceUtils.getLoveApp() != -1) {
                    //Don't love app
                    int random = new Random().nextInt(100);
                    //int clonedCnt = CloneHelper.getInstance(this).getClonedApps().size();
                    boolean isShowRateDialog = PreferenceUtils.getLoveApp() == 1 ||
                            ((random < RemoteConfig.getLong(QUIT_RATE_RANDOM)));
                    if (isShowRateDialog && (System.currentTimeMillis() - lastTime) > interval) {
                        showRate = true;
                        showRateDialog(RATE_FROM_QUIT);
                    }
                }
                if (!showRate) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public void onNavigationClick(View view) {
        int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
        if (drawer.isDrawerVisible(GravityCompat.START)
                && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

////////ADs start
    public static AdSize getBannerSize() {
        return AdSize.MEDIUM_RECTANGLE;
    }

    private void inflateHomeNativeAd(IAdAdapter ad) {
        if (ad == null) {
            return;
        }

        ViewGroup adContainer = findViewById(R.id.ad_container);
        final AdViewBinder viewBinder  =  new AdViewBinder.Builder(R.layout.home_native_ad_layout)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainMediaId(R.id.ad_cover_image)
                .fbMediaId(R.id.ad_fb_mediaview)
                .admMediaId(R.id.ad_adm_mediaview)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationId(R.id.ad_choices_container)
                .build();
        View adView = ad.getAdView(this,viewBinder);
        if (adView != null) {
            if (adView.getParent() != null) {
                ((ViewGroup)adView.getParent()).removeView(adView);
            }
            adContainer.removeAllViews();
            adContainer.addView(adView);
            adContainer.setVisibility(View.VISIBLE);
        }
    }

    public static void preloadAd(Context context) {
        if (!FlashUser.getInstance().isVIP()) {
            FuseAdLoader.get(SLOT_HOME_BANNER, context).
                    setBannerAdSize(getBannerSize()).preloadAd(context);
            if (!LocalVpnService.IsRunning) {
                FuseAdLoader.get(SLOT_CONNECTED_AD, context).preloadAd(context);
            }
            FlashUser.getInstance().preloadRewardVideoTask();
        }
    }

    private void loadHomeNativeAds() {
        if (!FlashUser.getInstance().isVIP()) {
            loadHomeNativeAd();
        }
    }

    private void loadHomeNativeAd() {
        FuseAdLoader.get(SLOT_HOME_BANNER, this).setBannerAdSize(getBannerSize()).
                loadAd(this, 2, 2000, new IAdLoadListener() {
                    @Override
                    public void onAdLoaded(IAdAdapter ad) {
                        inflateHomeNativeAd(ad);
                    }

                    @Override
                    public void onAdClicked(IAdAdapter ad) {

                    }

                    @Override
                    public void onAdClosed(IAdAdapter ad) {

                    }

                    @Override
                    public void onAdListLoaded(List<IAdAdapter> ads) {

                    }

                    @Override
                    public void onError(String error) {

                    }

                    @Override
                    public void onRewarded(IAdAdapter ad) {

                    }
                });
    }
////////ADs end

////Misc below
    private boolean rateDialogShowed = false;
    private void showRateDialog(final String from){
        if (rateDialogShowed ) {
            MLogs.d("Already showed dialog this time");
            return;
        }
        rateDialogShowed= true;
        EventReporter.reportRate(this,"start", from);
        PreferenceUtils.updateRateDialogTime(this);
        final RateDialog rateDialog = new RateDialog(this, from);
        rateDialog.show().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EventReporter.reportRate(HomeActivity.this, from+"_cancel", from);
                PreferenceUtils.setLoveApp(false);
                rateDialogShowed = false;
            }
        });
    }

    private void showUpdateDialog() {
        EventReporter.generalEvent(this, "update_dialog");
        UpDownDialog.show(this, this.getResources().getString(R.string.update_dialog_title),
                this.getResources().getString(R.string.update_dialog_content, "" + RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION)),
                this.getResources().getString(R.string.update_dialog_left), this.getResources().getString(R.string.update_dialog_right),
                -1, R.layout.dialog_up_down,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                dialogInterface.dismiss();
                                PreferenceUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                dialogInterface.dismiss();
                                String forceUpdateUrl = RemoteConfig.getString("force_update_to");
                                if (!TextUtils.isEmpty(forceUpdateUrl)) {
                                    CommonUtils.jumpToUrl(HomeActivity.this,forceUpdateUrl);
                                } else {
                                    CommonUtils.jumpToMarket(HomeActivity.this, getPackageName());
                                }
                                EventReporter.generalEvent(HomeActivity.this, "update_go");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                PreferenceUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
            }
        });
    }
}
