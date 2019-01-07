package com.mobile.earnings.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.BaseActivity;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.modules.SharingModule;
import com.mobile.earnings.api.responses.DefaultTaskResponse;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;
import com.mobile.earnings.autorization.VkLoginCallback;
import com.mobile.earnings.main.ali_promo.AliPromoService;
import com.mobile.earnings.main.ali_promo.TimerUpdateHandler;
import com.mobile.earnings.main.fragments.FundsFragment;
import com.mobile.earnings.main.fragments.InfoFragment;
import com.mobile.earnings.main.fragments.ReferralScreen;
import com.mobile.earnings.main.fragments.TasksFragment;
import com.mobile.earnings.main.presenterImpls.MainActPresenterImpl;
import com.mobile.earnings.main.views.MainActView;
import com.mobile.earnings.profile.ProfileActivity;
import com.mobile.earnings.utils.Constantaz;
import com.mobile.earnings.utils.TextUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.vk.sdk.VKSdk;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mobile.earnings.main.adapters.BaseTasksAdapter.VIEW_TYPE_DEFAULT_TASK;
import static com.mobile.earnings.main.ali_promo.AliPromoService.EXTRA_ALI_SERVICE_END_TIME;
import static com.mobile.earnings.main.ali_promo.TimerUpdateHandler.MESSAGE_UPDATE_TIME;
import static com.mobile.earnings.single.task.DetailedTaskActivity.getDetailedTaskIntent;
import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_ACTIVE;
import static com.mobile.earnings.utils.Constantaz.EXTRA_OPEN_DETAILED;
import static com.mobile.earnings.utils.Constantaz.PREFS_FIRST_PROMO;
import static com.mobile.earnings.utils.Constantaz.REQUEST_CODE_PROMO_TASK;
import static com.mobile.earnings.utils.GlobalRandomize.randomize;

public class MainActivity extends BaseActivity implements MainActView, BaseActivity.ToolbarClickHelper, ServiceConnection, RewardedVideoAdListener, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.timer_title)
    TextView     timerTitleTv;
    @BindView(R.id.timerView)
    LinearLayout timerView;
    @BindView(R.id.timer_reward)
    TextView     timerRewardTv;
    @BindView(R.id.timer_getMoneyBut)
    Button       getRewardBut;
    @BindView(R.id.loadingWidget)
    FrameLayout  loadingWidget;
    @BindView(R.id.tool_bar)
    Toolbar      toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout   drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private TextView tvWaitBalance, tvBalance, tvReferrals;

    private MainActPresenterImpl presenter;
    private RewardedVideoAd      adMob;
    public  float                mainBalance, expectedBalance;
    private String currencyCode = "/u20BD";
    private ArrayList<AppModel> mTaskList, mActiveTaskList, mDefaultTaskList;

    private SendCopyAliPromoCode mBundle;
    private TimerUpdateHandler mTimerUpdateHandler = new TimerUpdateHandler(this);
    private Intent          mTimerIntent;
    private int             mTimerCountdownTime;
    private AliPromoService mAliPromoService;
    private boolean mIsAliPromoServicesBound = false;
    private ActionBarDrawerToggle toggle;

    public static Intent getMainIntent(@NonNull Context context, boolean isReminder, int appId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_OPEN_ACTIVE, isReminder);
        intent.putExtra(EXTRA_OPEN_DETAILED, appId);
        return intent;
    }

    public static Intent getMainIntent(@NonNull Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setToolBar(getResources().getString(R.string.main_toolbar_title));
        presenter = new MainActPresenterImpl(this);
        presenter.updateUserData();
        View navHeader = navigationView.getHeaderView(0);
        tvWaitBalance = (TextView) navHeader.findViewById(R.id.nav_wait_balance);
        tvBalance = (TextView) navHeader.findViewById(R.id.nav_balance);
        tvReferrals = (TextView) navHeader.findViewById(R.id.nav_referrals);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleStartIntent();
        reBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unBoundService();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setDataFromServer(float ownBalance, float expectedBalance,
                                  String currencyCode, int referralCount) {
        mainBalance = ownBalance;
        this.expectedBalance = expectedBalance;
        this.currencyCode = currencyCode;
        currencyCode = StringEscapeUtils.unescapeJava("\\" + currencyCode);
        String formattedBalance = (" " + mainBalance + " ").concat(currencyCode);
        tvBalance.setText(formattedBalance);
        tvWaitBalance.setText((" " + expectedBalance + " ").concat(currencyCode));
        tvReferrals.setText(" " + referralCount);
        presenter.getTasks();
    }

    @Override
    public void setToolbarTitle(String toolbarTitle) {
        updateToolbarTitle(toolbarTitle);
    }

    @Override
    public void setToolbarBalance() {
        currencyCode = StringEscapeUtils.unescapeJava("\\" + currencyCode);
        String formattedBalance = (" " + mainBalance).concat(currencyCode);
        tvBalance.setText(formattedBalance);
        tvWaitBalance.setText((" " + expectedBalance).concat(currencyCode));
    }

    @Override
    public void informUser(int resourceId) {
        Toast.makeText(App.getContext(), App.getContext().getString(resourceId), Toast.LENGTH_LONG).show();
    }

    @Override
    public void informUser(String message) {
        Toast.makeText(App.getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void initTaskList(ArrayList<AppModel> tasks) {
        mTaskList = tasks;
        presenter.getActiveTasks();
    }

    @Override
    public void initActiveList(ArrayList<AppModel> activeTasks) {
        mActiveTaskList = activeTasks;
        presenter.getDefaultTaskPrices();
    }

    @Override
    public void initDefaultList(DefaultTaskResponse.Settings data) {
        mDefaultTaskList = provideDefaultTasks(data);
        initDrawer();
    }

    @Override
    public void openDetailedTaskActivity(AppModel model) {
        startActivity(getDetailedTaskIntent(this, model, true));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        VkLoginCallback vkLoginCallback = new VkLoginCallback();
        if (requestCode == REQUEST_CODE_PROMO_TASK && data != null) {
            mBundle = (SendCopyAliPromoCode) data.getSerializableExtra(Constantaz.EXTRA_PROMO_BUDNLE);
            mTimerIntent = new Intent(MainActivity.this, AliPromoService.class);
            mTimerCountdownTime = (int) (mBundle.timerTimeInMillis / 1000);
            mTimerIntent.putExtra(EXTRA_ALI_SERVICE_END_TIME, mTimerCountdownTime);
            startService(mTimerIntent);
            bindService(mTimerIntent, this, 0);
        } else if (!VKSdk.onActivityResult(requestCode, resultCode, data, vkLoginCallback)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            String promoCode = App.getPrefs().getString(PREFS_FIRST_PROMO, "");
            SharingModule.vkShare(String.format(getResources().getString(R.string.share_message), promoCode) + getResources().getString(R.string.share_link));
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(App.getContext(), App.getContext().getString(R.string.videoIsReadyMessage), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.i("AD_MOB", "onRewardedVideoAdOpened: ");
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.i("AD_MOB", "onRewardedVideoStarted: ");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.i("AD_MOB", "onRewardedVideoAdClosed: ");
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        presenter.payForVideoAd(randomize());
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.i("AD_MOB", "onRewardedVideoAdLeftApplication: ");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.e("AD_MOB", "Error: " + i);
    }

    @Override
    public void onElementClick(int tabPosition) {
        openTab(tabPosition);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean updateTimer() {
        int seconds = mAliPromoService.getSecondsLeft();
        boolean shouldUpdate = seconds > 0;
        if (mAliPromoService.isServiceForeground()) {
            mAliPromoService.updateNotification(seconds);
        } else {
            if (shouldUpdate) {
                timerTitleTv.setText(Html.fromHtml(getString(R.string.timer_title, seconds)));
            } else {
                onTimerFinished();
            }
        }
        return shouldUpdate;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AliPromoService.AliPromoBinder binder = (AliPromoService.AliPromoBinder) service;
        mAliPromoService = binder.getService();
        mIsAliPromoServicesBound = true;
        mAliPromoService.runBackground();
        enableAliTimer();
        if (!mAliPromoService.isTimerRunning()) {
            mAliPromoService.startTimer();
            mTimerUpdateHandler.sendEmptyMessage(MESSAGE_UPDATE_TIME);
        } else if (mAliPromoService.getSecondsLeft() <= 0) {
            onTimerFinished();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mIsAliPromoServicesBound = false;
    }

    @Override
    public void onTimerFinished() {
        timerTitleTv.setText(getString(R.string.timer_resultTitle));
        getRewardBut.setEnabled(true);
        getRewardBut.setClickable(true);
        getRewardBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.disablePromoTask(mBundle);
                timerView.setVisibility(View.GONE);
                stopService(mTimerIntent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        openTab(item.getItemId());
        return true;
    }

    @Override
    public void showLoading() {
        loadingWidget.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingWidget.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initDrawer() {
        toggle = new ActionBarDrawerToggle(this,  /* host activity */
                drawer,  /* Drawer Layout object */
                toolbar,  /* Toolbar Object */
                R.string.nav_open,  /* description for accessibility */
                R.string.nav_closed  /* description for accessibility */);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        openTab(R.id.nav_tasks);
    }

    private void handleStartIntent() {
        final int appId = getIntent().getIntExtra(EXTRA_OPEN_DETAILED, -1);
        if (appId != -1) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.getAppModel(appId);
                }
            }, 500);
        } else {
            initAdMob();
            presenter.getFundsFromServer();
        }
    }

    private void initAdMob() {
        MobileAds.initialize(this, Constantaz.AD_MOB_ID);
        adMob = MobileAds.getRewardedVideoAdInstance(this);
        adMob.setRewardedVideoAdListener(this);
        adMob.loadAd(Constantaz.AD_MOB_UNIT, new AdRequest.Builder().build());
    }

    public void showAdMob() {
        if (adMob.isLoaded()) {
            adMob.show();
        } else {
            informUser(R.string.videoNotReadyError);
        }
    }

    private void openTab(int itemId) {
        switch (itemId) {
            case R.id.nav_tasks:
                replaceFragment(TasksFragment.getInstance(mActiveTaskList, mTaskList, mDefaultTaskList));
                setToolbarBalance();
                break;
            case R.id.nav_balance:
                replaceFragment(FundsFragment.getInstance());
                setToolbarTitle(getString(R.string.money_toolbar_title));
                break;
            case R.id.nav_friends:
                replaceFragment(ReferralScreen.getInstance(ReferralScreen.FIRST_PAGE));
                setToolbarTitle(getString(R.string.toolbarTitle_friends));
                break;
            case R.id.nav_account:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.nav_info:
                replaceFragment(InfoFragment.newInstance());
                setToolbarTitle(getString(R.string.rules_toolbar_title));
                break;
            default:
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    private void enableAliTimer() {
        if (mIsAliPromoServicesBound) {
            timerView.setVisibility(View.VISIBLE);
            timerRewardTv.setText(mBundle.rewardPrice);
            getRewardBut.setEnabled(false);
            getRewardBut.setClickable(false);
        }
    }

    private void reBindService() {
        if (mAliPromoService != null) {
            mTimerIntent = new Intent(this, AliPromoService.class);
            mTimerIntent.putExtra(EXTRA_ALI_SERVICE_END_TIME, mTimerCountdownTime);
            startService(mTimerIntent);
            bindService(mTimerIntent, this, 0);
        }
    }

    private void unBoundService() {
        if (mIsAliPromoServicesBound) {
            if (mAliPromoService != null && mAliPromoService.isTimerRunning()) {
                mAliPromoService.runForeground();
            }
            unbindService(this);
            mIsAliPromoServicesBound = false;
        }
    }

    private ArrayList<AppModel> provideDefaultTasks(@NonNull DefaultTaskResponse.Settings data) {
        ArrayList<AppModel> tempList = new ArrayList<>();
        int[] defIds = getResources().getIntArray(R.array.defTaskIds);
        String[] taskTitles = getResources().getStringArray(R.array.defTasksTitles);
        String[] taskLinks = getResources().getStringArray(R.array.defTasksLinks);
        String[] prices = presenter.extractDefaultPrices(data);
        TypedArray taskIcons = getResources().obtainTypedArray(R.array.defTasksIcons);
        // FIXME: 13.07.17 Can be bug - when retrieve task from server with id = 0
        for (int i = 0; i < taskTitles.length; i++) {
            tempList.add(new AppModel(defIds[i], VIEW_TYPE_DEFAULT_TASK, taskTitles[i], taskLinks[i], prices[i], data.currency, taskIcons.getResourceId(i, -1)));
        }
        //adding one more task if it available
        if (data.isDefaultAppReviewAvailable) {
            AppModel model = new AppModel(DefTaskIds.DEF_COMMEND, VIEW_TYPE_DEFAULT_TASK, getString(R.string.def_commend_task_title), getString(R.string.def_commend_task_link), String.valueOf(data.commentAward), data.currency, R.mipmap.ic_launcher);
            model.defaultTaskKeywords = TextUtils.provideKeywords(data.commentKeywords);
            tempList.add(model);
        }
        taskIcons.recycle();
        return tempList;
    }

}
