package com.polestar.superclone.reward;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.imageloader.TaskManager;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.component.activity.HomeActivity;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.datamodels.AdTask;
import com.polestar.task.database.datamodels.RewardVideoTask;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2019/1/23.
 * TODO
 * 1. 切国家邀请问题
 * 2. 价格波动提醒
 * 3. 只有安装时间小于1天内的用户才能填邀请码
 * 4. finishTask被限流后没有回调
 * 5. 请求带app版本
 */



public class RewardCenterFragment extends BaseFragment
        implements AppUser.IUserUpdateListener, View.OnClickListener{
    private View contentView;
    private View inviteItemView;
    private View checkinItemView;
    private View videoItemView;
    private View userInfoView;
    private ListView adTaskListView;
    private BaseAdapter adTaskAdapter;
    private AppUser appUser;
    private ProgressBar loadingProgressBar;
    private ProgressBar taskRunningProgressBar;
    private LinearLayout loadFailLayout;
    private LinearLayout loadedLayout;
    private Handler mainHandler;
    private View retryView;
    private TaskExecutor mTaskExecutor;
    private FuseAdLoader nativeAdLoader;
    private FuseAdLoader chechInAdLoader;
    private IAdAdapter nativeAd;
    private List<IAdAdapter> taskAdList;

    private static final int MSG_LOAD_TIMEOUT = 100;
    private static final long LOAD_TIMEOUT = 10*1000;

    public static final String SLOT_REWARD_CENER_NATIVE = "slot_reward_center_native";
    public static final String SLOT_CHECKIN_INTERSTITIAL = "slot_checkin_interstitial";
    public static final String SLOT_TASK_AD_LIST = "slot_reward_ad_list";

    public static AdSize getBannerSize() {
        int dpWidth = DisplayUtils.px2dip(MApp.getApp(), DisplayUtils.getScreenWidth(MApp.getApp()));
        dpWidth = dpWidth < 290 ? dpWidth : dpWidth-10;
        return new AdSize(dpWidth, 135);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nativeAd = null;
        appUser = AppUser.getInstance();
        appUser.listenOnUserUpdate(this);
        mTaskExecutor = new TaskExecutor(mActivity);
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LOAD_TIMEOUT:
                        loadedLayout.setVisibility(View.GONE);
                        loadingProgressBar.setVisibility(View.GONE);
                        loadFailLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.reward_center_layout, null);
            initView();
        }
        MLogs.d(" reward onCreateView");
        initData();
//        if (nativeAd == null) {
        if (!PreferencesUtils.isAdFree()) {
            nativeAdLoader = FuseAdLoader.get(SLOT_REWARD_CENER_NATIVE, mActivity);
            nativeAdLoader.setBannerAdSize(getBannerSize());
            nativeAdLoader.loadAd(mActivity, 2, 1000, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    nativeAd = ad;
                    if (ad != null && mActivity != null) {
                        MLogs.d("reward loaded ad");
                        ViewGroup adContainer = contentView.findViewById(R.id.ad_container);
                        adContainer.setVisibility(View.VISIBLE);
                        AdViewBinder viewBinder = new AdViewBinder.Builder(R.layout.native_ad_reward_center)
                                .titleId(R.id.ad_title)
                                .textId(R.id.ad_subtitle_text)
                                .mainMediaId(R.id.ad_cover_image)
                                .fbMediaId(R.id.ad_fb_mediaview)
                                .admMediaId(R.id.ad_adm_mediaview)
                                .iconImageId(R.id.ad_icon_image)
                                .callToActionId(R.id.ad_cta_text)
                                .privacyInformationId(R.id.ad_choices_image)
                                .adFlagId(R.id.ad_flag)
                                .build();
                        View adView = ad.getAdView(mActivity, viewBinder);
                        if (adView != null) {
                            adContainer.removeAllViews();
                            adContainer.addView(adView);
                            adContainer.setVisibility(View.VISIBLE);
                        }
                    }
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
        } else {
            ViewGroup adContainer = contentView.findViewById(R.id.ad_container);
            adContainer.setVisibility(View.GONE);
        }
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        taskRunningProgressBar.setVisibility(View.GONE);
        MLogs.d(" reward onResume");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    private void initView() {
        userInfoView = contentView.findViewById(R.id.reward_user_info_layout);
        View store = userInfoView.findViewById(R.id.store_button);
        store.setOnClickListener(this);
        inviteItemView = contentView.findViewById(R.id.invite_task_item);
        checkinItemView = contentView.findViewById(R.id.checkin_task_item);
        videoItemView = contentView.findViewById(R.id.video_task_item);
        loadFailLayout = contentView.findViewById(R.id.loading_fail_layout);
        loadingProgressBar = contentView.findViewById(R.id.loading_layout);
        taskRunningProgressBar = contentView.findViewById(R.id.task_executing_layout);
        loadedLayout = contentView.findViewById(R.id.loaded_layout);
        retryView = contentView.findViewById(R.id.retry);
        retryView.setOnClickListener(this);
        adTaskListView = contentView.findViewById(R.id.ad_task_list);
    }

    private void initData() {
        if (appUser.isRewardAvailable()) {
            loadingProgressBar.setVisibility(View.GONE);
            loadFailLayout.setVisibility(View.GONE);
            loadedLayout.setVisibility(View.VISIBLE);
            updateUserInfo();
            initTaskItems();
        } else {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadFailLayout.setVisibility(View.GONE);
            loadedLayout.setVisibility(View.GONE);
            appUser.forceRefreshData();
            mainHandler.sendEmptyMessageDelayed(MSG_LOAD_TIMEOUT, LOAD_TIMEOUT);
        }
    }

    @Override
    public void onUserDataUpdated() {
        initData();
    }

    private void updateUserInfo() {
        float balance = appUser.getMyBalance();
        TextView points = userInfoView.findViewById(R.id.user_balance_txt);
        if(balance == 0) {
            points.setText(getText(R.string.finish_task_get_reward));
        } else {
            points.setText(String.format(mActivity.getString(R.string.you_have_coins), balance , mActivity.getString(R.string.coin_unit)));
        }
    }

    private void initTaskItems(){
        updateTaskViewItem(inviteItemView, appUser.getInviteTask(), true);
        updateTaskViewItem(checkinItemView, appUser.getCheckInTask(), true);
        updateTaskViewItem(videoItemView, appUser.getVideoTask(), true);
        adTaskAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                int count = taskAdList == null ? 0: taskAdList.size();
                MLogs.d("getCount: " + count);
                return count;
            }

            @Override
            public Object getItem(int position) {
                return taskAdList == null ? null: taskAdList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AdViewBinder viewBinder = new AdViewBinder.Builder(R.layout.adtask_item)
                        .titleId(R.id.task_title)
                        .iconImageId(R.id.task_icon).build();
                IAdAdapter adAdapter = (IAdAdapter) getItem(position);
                View adView = adAdapter.getAdView(mActivity,viewBinder);
                TextView taskDesc = adView.findViewById(R.id.task_description);
                AdTask adTask = ((AdTask)adAdapter.getAdObject());
                taskDesc.setText(((AdTask)adAdapter.getAdObject()).mDescription);
                TextView reward = adView.findViewById(R.id.task_reward);
                reward.setText("+" + String.format("%.0f", adTask.mPayout));
                reward.setTextColor(getResources().getColor(R.color.reward_collect_coin_color));
                return adView;
            }
        };
        adTaskListView.setAdapter(adTaskAdapter);
        FuseAdLoader.get(SLOT_TASK_AD_LIST, mActivity).loadAdList(mActivity, 10, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {

            }

            @Override
            public void onAdClicked(IAdAdapter ad) {

            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {
                taskAdList = ads;
                adTaskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {

            }

            @Override
            public void onRewarded(IAdAdapter ad) {

            }
        });
    }

    private void updateTaskViewItem(View view, Task task, boolean isInit){
        if (view == null || task == null) {
            return;
        }
        TextView title = view.findViewById(R.id.task_title);
        TextView description = view.findViewById(R.id.task_description);
        IconFontTextView icon = view.findViewById(R.id.task_icon);
        IconFontTextView reward = view.findViewById(R.id.task_reward);
        title.setText(task.mTitle);
        description.setText(task.mDescription);
        view.setTag(task);
        view.setOnClickListener(this);
        switch (task.mTaskType) {
            case Task.TASK_TYPE_CHECKIN_TASK:
                icon.setText((R.string.iconfont_checkin));
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, getResources().getColor(R.color.checkin_task_btn));
                break;
            case Task.TASK_TYPE_SHARE_TASK:
                icon.setText((R.string.iconfont_invite));
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, getResources().getColor(R.color.share_task_btn));
                break;
            case Task.TASK_TYPE_REWARDVIDEO_TASK:
                icon.setText((R.string.iconfont_video));
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, getResources().getColor(R.color.checkin_task_btn));
                break;
        }
        int status = TaskExecutor.checkTask(task);
        MLogs.d("task " + task.mTitle + " status: " + status);
        if (status == RewardErrorCode.TASK_EXCEED_DAY_LIMIT) {
//            reward.setText(R.string.iconfont_wait);
//            reward.setTextColor(getResources().getColor(R.color.reward_wait));
//        } else if (status == RewardErrorCode.TASK_EXCEED_DAY_LIMIT && !isInit) {
            reward.setText(R.string.iconfont_done);
            reward.setTextColor(getResources().getColor(R.color.reward_done));
        }  else {
            reward.setText("+" + String.format("%.0f", task.mPayout));
            if (status != RewardErrorCode.TASK_OK) {
                reward.setTextColor(getResources().getColor(R.color.text_gray_light));
            } else {
                if (task.mTaskType == Task.TASK_TYPE_CHECKIN_TASK) {
                    chechInAdLoader = FuseAdLoader.get(SLOT_CHECKIN_INTERSTITIAL, mActivity);
                    chechInAdLoader.loadAd(mActivity, 2, 1000, null);
                }
                reward.setTextColor(getResources().getColor(R.color.reward_collect_coin_color));
            }
        }
    }

    public void onStoreClick(View view){
        MLogs.d("onStoreClick");
        ((HomeActivity)mActivity).doSwitchToStoreFragment();
//        ProductsActivity.start(getActivity());
    }

    public void onRetryClick(View view) {
        loadedLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadFailLayout.setVisibility(View.GONE);
        mainHandler.sendEmptyMessageDelayed(MSG_LOAD_TIMEOUT, LOAD_TIMEOUT);
        appUser.forceRefreshData();
    }

    public void onRewardVideoClick(View view) {
        RewardVideoTask task = (RewardVideoTask) view.getTag();
        if (task != null) {
            FuseAdLoader loader = FuseAdLoader.get(task.adSlot, mActivity);
            if (loader == null) {
                MLogs.d("Wrong adSlot config in task " + task.toString());
                RewardErrorCode.toastMessage(mActivity, RewardErrorCode.TASK_UNEXPECTED_ERROR);
                return;
            }
            if(! appUser.isRewardVideoTaskReady() ) {
                taskRunningProgressBar.setVisibility(View.VISIBLE);
            }
            mTaskExecutor.execute(task, new RewardTaskListener(view));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry:
                onRetryClick(v);
                break;
            case R.id.store_button:
                onStoreClick(v);
                break;
            case R.id.checkin_task_item:
            case R.id.invite_task_item:
                mTaskExecutor.execute((Task)v.getTag(), new RewardTaskListener(v));
                break;
            case R.id.video_task_item:
                onRewardVideoClick(v);
                break;
        }
    }

    private class RewardTaskListener implements ITaskStatusListener {
        private View mView;

        public RewardTaskListener(View view) {
            mView = view;
        }

        @Override
        public void onTaskSuccess(long taskId, float payment, float balance) {
            MLogs.d(taskId + " Task finish : "  + payment + " balance " + balance);
            updateUserInfo();
            Task task = (Task)mView.getTag();
            try {
                updateTaskViewItem(mView, task, false);
                RewardErrorCode.toastMessage(mActivity, RewardErrorCode.TASK_OK, payment);
                taskRunningProgressBar.setVisibility(View.GONE);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
            if (task.mTaskType == Task.TASK_TYPE_CHECKIN_TASK) {
                chechInAdLoader = FuseAdLoader.get(SLOT_CHECKIN_INTERSTITIAL, mActivity);
                chechInAdLoader.loadAd(mActivity, 2, 100, new IAdLoadListener() {
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
        }

        @Override
        public void onTaskFail(long taskId, ADErrorCode code) {
            RewardErrorCode.toastMessage(mActivity, code.getErrCode());
            taskRunningProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            RewardErrorCode.toastMessage(mActivity, code.getErrCode());
            taskRunningProgressBar.setVisibility(View.GONE);
        }
    }
}
