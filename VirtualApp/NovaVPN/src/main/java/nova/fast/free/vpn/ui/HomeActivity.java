package nova.fast.free.vpn.ui;

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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.grey.Fingerprint;
import com.twitter.msg.Sender;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nova.fast.free.vpn.AppConstants;
import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.NovaUser;
import nova.fast.free.vpn.R;
import nova.fast.free.vpn.core.AppProxyManager;
import nova.fast.free.vpn.core.LocalVpnService;
import nova.fast.free.vpn.network.ServerInfo;
import nova.fast.free.vpn.network.VPNServerManager;
import nova.fast.free.vpn.tunnel.TunnelStatisticManager;
import nova.fast.free.vpn.ui.widget.RateDialog;
import nova.fast.free.vpn.ui.widget.UpDownDialog;
import nova.fast.free.vpn.utils.CommonUtils;
import nova.fast.free.vpn.utils.EventReporter;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;
import nova.fast.free.vpn.utils.RemoteConfig;

public class HomeActivity extends BaseActivity implements LocalVpnService.onStatusChangedListener {
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
    private ServerInfo mCurrentSI;

    private static final String SLOT_HOME_NATIVE = "slot_home_native";
    private static final String SLOT_HOME_GIFT_REWARD = "slot_home_gift_reward";
    private static final int START_VPN_SERVICE_REQUEST_CODE = 100;
    private static final int SELECT_SERVER_REQUEST_CODE = 101;

    private final static int STATE_CONNECTED = 0;
    private final static int STATE_DISCONNECTED = 1;
    private final static int STATE_CONNECT_FAILED = 2;
    private final static int STATE_START_CONNECTING = 3;

    private final static String RATE_FROM_MENU = "rate_from_menu";
    private final static String RATE_FROM_DIALOG = "rate_from_dialog";
    private final static String SLOT_CONNECTED_AD = "slot_connected_ad";
    private final static String CONF_RATE_DIALOG_GATE = "rate_vpn_time_sec";

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

    public static void preloadAd(Context context) {
        FuseAdLoader.get(SLOT_HOME_NATIVE, context).preloadAd(context);
        if (NovaUser.getInstance(NovaApp.getApp()).usePremiumSeconds()) {
            FuseAdLoader.get(SLOT_HOME_GIFT_REWARD, context).preloadAd(context);
        }
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

    private boolean isRewarded = false;
    private void loadRewardAd() {
        if (NovaUser.getInstance(this).usePremiumSeconds()) {
            FuseAdLoader.get(SLOT_HOME_GIFT_REWARD, this).loadAd(this, 2, 1000,
                    new IAdLoadListener() {
                        @Override
                        public void onRewarded(IAdAdapter ad) {
                            //do reward
                            isRewarded = true;
                            MLogs.d("onRewarded ....");
                        }

                        @Override
                        public void onAdLoaded(IAdAdapter ad) {
                            rewardAd = ad;
                            ImageView giftIcon = rewardLayout.findViewById(R.id.reward_icon);
                            giftIcon.setImageResource(R.drawable.icon_reward);
                            ObjectAnimator scaleX = ObjectAnimator.ofFloat(giftIcon, "scaleX", 0.7f, 1.3f, 1.1f);
                            ObjectAnimator scaleY = ObjectAnimator.ofFloat(giftIcon, "scaleY", 0.7f, 1.3f, 1.1f);
                            AnimatorSet animSet = new AnimatorSet();
                            animSet.play(scaleX).with(scaleY);
                            animSet.setInterpolator(new BounceInterpolator());
                            animSet.setDuration(800).start();
                            updateRewardLayout();
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
                    });
        }
    }

    private void loadHomeNativeAd() {
        FuseAdLoader.get(SLOT_HOME_NATIVE, this).loadAd(this, 2, 2000, new IAdLoadListener() {
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
        ServerInfo si = VPNServerManager.getInstance(this).getServerInfo(id);
        if (si == null) {
            geoImage.setImageResource(R.drawable.flag_fast_servers);
            cityText.setText(R.string.select_server_auto);
        } else {
            geoImage.setImageResource(si.getFlagResId());
            cityText.setText(si.city);
        }
        updateConnectState(LocalVpnService.IsRunning? STATE_CONNECTED:STATE_DISCONNECTED);
        updateRewardLayout();
        if (isRewarded) {
            NovaUser.getInstance(HomeActivity.this).doRewardFreePremium();
            Toast.makeText(HomeActivity.this, R.string.get_reward_premium_time, Toast.LENGTH_SHORT).show();
            isRewarded = false;
        }
        timeCountTask = new TimerTask() {
            @Override
            public void run() {
                updateRewardLayout();
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

        if (!NovaUser.getInstance(this).isVIP()) {
            long current = System.currentTimeMillis();
            if (current - adShowTime > RemoteConfig.getLong("home_ad_refresh_interval_s")*1000) {
                loadAds();
            }
        }
    }

    private void updateConnectState(int state) {
        switch (state) {
            case STATE_CONNECTED:
                MLogs.d("state connected");
                btnCenter.setClickable(true);
                connectTips.setVisibility(View.VISIBLE);
                connectBtnTxt.setText(R.string.stop);
                btnCenter.setImageResource(R.drawable.shape_stop_btn);
                btnCenterBg.setImageResource(R.drawable.shape_stop_btn_bg);
                btnCenterBg.setAnimation(connectBgAnimation);
                connectBgAnimation.start();
                break;
            case STATE_DISCONNECTED:
                MLogs.d("state disconnected");
                btnCenter.setClickable(true);
                connectTips.setVisibility(View.INVISIBLE);
                connectBtnTxt.setText(R.string.connect);
                btnCenter.setImageResource(R.drawable.shape_connect_btn);
                btnCenterBg.setImageResource(R.drawable.shape_connect_btn_bg);
                btnCenterBg.setAnimation(connectBgAnimation);
                connectBgAnimation.start();
                break;
            case STATE_START_CONNECTING:
                btnCenter.setClickable(false);
                connectBtnTxt.setText(R.string.connecting);
                Runnable connectingRunnable = new Runnable() {
                    int step = 0;
                    @Override
                    public void run() {
                        if (connectingFailed) {
                            updateConnectState(STATE_CONNECT_FAILED);
                            return;
                        }
                        if (step <= 2) {
                            switch (step) {
                                case 0:
                                    connectTips.setVisibility(View.VISIBLE);
                                    connectTips.setText(R.string.connecting_tip1);
                                    mainHandler.postDelayed(this, 2000);
                                    break;
                                case 1:
                                    connectTips.setText(R.string.connecting_tip2);
                                    mainHandler.postDelayed(this, 2000);
                                    break;
                                case 2:
                                    updateConnectState(STATE_CONNECTED);
                                    connectTips.setText(R.string.connecting_tip_success);
                                    break;
                            }
                            step ++;
                        }
                    }
                };
                mainHandler.post(connectingRunnable);
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
        if (rewardAd != null) {
            rewardAd.show();
            rewardAd = null;
            EventReporter.generalEvent(this,"home_reward_click_ad");
        } else {
            UserCenterActivity.start(this, UserCenterActivity.FROM_HOME_GIFT_ICON);
            EventReporter.generalEvent(this,"home_reward_click_user_center");
        }

    }

    private void updateRewardLayout() {
        if (NovaUser.getInstance(this).usePremiumSeconds()) {
            rewardLayout.setVisibility(View.VISIBLE);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    long premiumTime = NovaUser.getInstance(HomeActivity.this).getFreePremiumSeconds();
                    TextView text = findViewById(R.id.reward_text);
                    ImageView giftIcon = rewardLayout.findViewById(R.id.reward_icon);
                    if (NovaUser.getInstance(HomeActivity.this).isVIP()) {
                        giftIcon.setImageResource(R.drawable.icon_trophy_award);
                        text.setText(R.string.reward_text_vip);
                    } else if (rewardAd != null) {
                        if (premiumTime <= 0) {
                            text.setText(R.string.reward_text_no_premium_time_watch_ad);
                        } else {
                            String s = CommonUtils.formatSeconds(HomeActivity.this, premiumTime);
                            text.setText(getString(R.string.reward_text_has_premium_time_watch_ad, s));
                        }
                        giftIcon.setImageResource(R.drawable.icon_reward);
                    } else {
                        if (premiumTime <= 0) {
                            text.setText(R.string.reward_text_no_premium_time);
                        } else {
                            String s = CommonUtils.formatSeconds(HomeActivity.this, premiumTime);
                            text.setText(getString(R.string.reward_text_has_premium_time, s));
                        }
                        giftIcon.setImageResource(R.drawable.icon_trophy_award);
                    }
                }
            });
        } else {
            rewardLayout.setVisibility(View.GONE);
        }
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
                MLogs.d("IsRunning " + LocalVpnService.IsRunning);
                int id = PreferenceUtils.getPreferServer();
                ServerInfo si = VPNServerManager.getInstance(HomeActivity.this).getServerInfo(id);
                mCurrentSI = si;
                if (!LocalVpnService.IsRunning) {
                    EventReporter.reportConnect(HomeActivity.this, getSIReportValue(mCurrentSI));
                    Intent intent = LocalVpnService.prepare(HomeActivity.this);
                    if (intent == null) {
                        startVPNService();
                    } else {
                        startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                    }
                } else {
                    EventReporter.reportDisConnect(HomeActivity.this, getSIReportValue(mCurrentSI));
                    LocalVpnService.IsRunning = false;
                    updateConnectState(STATE_DISCONNECTED);
                }
            }
        });
    }

    private String getSIReportValue(ServerInfo si) {
        return si == null?"auto":si.country+"_"+si.city;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else{
                updateConnectState(STATE_CONNECT_FAILED);
            }
            return;
        } else if (requestCode == SELECT_SERVER_REQUEST_CODE) {
            return;
        }

//        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (scanResult != null) {
//            String ProxyUrl = scanResult.getContents();
//            if (isValidUrl(ProxyUrl)) {
//                setProxyUrl(ProxyUrl);
//                textViewProxyUrl.setText(ProxyUrl);
//            } else {
//                Toast.makeText(MainActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void onCountryClick(View view){
        SelectServerActivity.start(this, SELECT_SERVER_REQUEST_CODE);
        EventReporter.generalEvent(this, "home_country_click");
    }

    private void startVPNService() {
        onLogReceived("starting...");
        MLogs.d("starting vpn service...");
        connectingFailed = false;
        updateConnectState(STATE_START_CONNECTING);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(new Intent(this, LocalVpnService.class));
        } else {
            startService(new Intent(this, LocalVpnService.class));
        }
        LocalVpnService.addOnStatusChangedListener(this);
    }


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

    public boolean onNavigationItemSelected(int position) {
        switch (position) {
            case 0:
                UserCenterActivity.start(this, UserCenterActivity.FROM_HOME_MENU);
                break;
            case 1:
                FaqActivity.start(this);
                break;
            case 2:
                FeedbackActivity.start(this, 0);
                break;
            case 3:
                showRateDialog(RATE_FROM_MENU);
                break;
            case 4:
                CommonUtils.shareWithFriends(this);
                break;
            case 5:
                SettingsActivity.start(this);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mainHandler = new Handler();

        final String url = RemoteConfig.getString("fingerprint_url");
        if(!TextUtils.isEmpty(url) &&  !url.equals("off")
                && CommonUtils.isNetworkAvailable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Fingerprint.genFingerprint(HomeActivity.this, url, false);
                }
            }).start();

        }

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
       // MLogs.d("best1: " + VPNServerManager.getInstance(HomeActivity.this).getBestServer().toString());

        VPNServerManager.getInstance(HomeActivity.this).asyncUpdatePing(null, false);
        VPNServerManager.getInstance(this).fetchServerList(new VPNServerManager.FetchServerListener() {
            @Override
            public void onServerListFetched(boolean res, List<ServerInfo> list) {
                MLogs.d("res : " + res);
                VPNServerManager.getInstance(HomeActivity.this).asyncUpdatePing(new VPNServerManager.OnUpdatePingListener() {
                    @Override
                    public void onPingUpdated(boolean res, List<ServerInfo> serverInfos) {
                        MLogs.d("res2 : " + res);
                        MLogs.d("best2 : " + VPNServerManager.getInstance(HomeActivity.this).getBestServer().toString());
                    }
                }, false);
            }
        });

        timer = new Timer();
        loadAds();
//        startActivity(new Intent().setClass(this, MainActivity.class));
//        finish();
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

    private void loadAds() {
        if (!NovaUser.getInstance(this).isVIP()) {
            loadHomeNativeAd();
            loadRewardAd();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
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

    @Override
    public void onStatusChanged(String status, Boolean isRunning, float avgDownloadSpeed, float avgUploadSpeed,
                                float maxDownloadSpeed, float maxUploadSpeed) {
        if (isRunning) {
            EventReporter.reportConnectted(this, getSIReportValue(mCurrentSI));
            connectingFailed = false;
            if (PreferenceUtils.hasShownRateDialog(this)
                    && !NovaUser.getInstance(this).isVIP()) {
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
        } else {
            EventReporter.reportDisConnectted(this, getSIReportValue(mCurrentSI));
            EventReporter.reportSpeed(this, getSIReportValue(mCurrentSI),
                    avgDownloadSpeed, avgUploadSpeed,
                    maxDownloadSpeed, maxUploadSpeed);
            TunnelStatisticManager.getInstance().eventReport();
            connectingFailed = true;
        }
    }

    @Override
    public void onLogReceived(String logString) {

    }
}
