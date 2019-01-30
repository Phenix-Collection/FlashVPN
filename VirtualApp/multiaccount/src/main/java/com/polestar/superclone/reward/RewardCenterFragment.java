package com.polestar.superclone.reward;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.component.activity.HomeActivity;
import com.polestar.superclone.utils.ColorUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
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



public class RewardCenterFragment extends BaseFragment implements AppUser.IUserUpdateListener, View.OnClickListener{
    private View contentView;
    private View inviteItemView;
    private View checkinItemView;
    private View videoItemView;
    private View userInfoView;
    private AppUser appUser;
    private ProgressBar loadingProgressBar;
    private ProgressBar taskRunningProgressBar;
    private LinearLayout loadFailLayout;
    private LinearLayout loadedLayout;
    private Handler mainHandler;
    private View retryView;

    private static final int MSG_LOAD_TIMEOUT = 100;
    private static final long LOAD_TIMEOUT = 10*1000;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.reward_center_layout, null);
        appUser = AppUser.getInstance();
        appUser.listenOnUserUpdate(this);
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
        initView();
        initData();
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        taskRunningProgressBar.setVisibility(View.GONE);
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
            points.setText(String.format(getString(R.string.you_have_coins), balance , getActivity().getString(R.string.coin_unit)));
        }
    }

    private void initTaskItems(){
        updateTaskViewItem(inviteItemView, appUser.getInviteTask(), true);
        updateTaskViewItem(checkinItemView, appUser.getCheckInTask(), true);
        updateTaskViewItem(videoItemView, appUser.getVideoTask(), true);
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
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, Color.parseColor("#4B57C0"));
                break;
            case Task.TASK_TYPE_SHARE_TASK:
                icon.setText((R.string.iconfont_invite));
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, Color.parseColor("#fd215c"));
                break;
            case Task.TASK_TYPE_REWARDVIDEO_TASK:
                icon.setText((R.string.iconfont_video));
                icon.setBackgroundShapeDrawable(IconFontTextView.BG_SHAPE_OVAL, Color.parseColor("#4B57C0"));
                break;
        }
        int status = appUser.checkTask(task);
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
               reward.setTextColor(getResources().getColor(R.color.reward_collect_coin_color));
            }
        }
    }

    public void onStoreClick(View view){
        MLogs.d("onStoreClick");
        ((HomeActivity)getActivity()).doSwitchToStoreFragment();
//        ProductsActivity.start(getActivity());
    }

    public void onRetryClick(View view) {
        loadedLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadFailLayout.setVisibility(View.GONE);
        mainHandler.sendEmptyMessageDelayed(MSG_LOAD_TIMEOUT, LOAD_TIMEOUT);
        appUser.forceRefreshData();
    }

    public void onCheckInClick(View view) {
        if (view.getTag() instanceof Task ) {
            int error = appUser.checkTask((Task) view.getTag());
            if (error != RewardErrorCode.TASK_OK) {
                toastError(error);
                return;
            }
        }
        appUser.finishTask((Task) view.getTag(), new RewardTaskListener(view));
    }

    public void onInviteFriendsClick(View view) {
        if (view.getTag() instanceof Task ) {
            int error = appUser.checkTask((Task) view.getTag());
            if (error != RewardErrorCode.TASK_OK) {
                toastError(error);
                return;
            }
        }
    }

    public void onRewardVideoClick(View view) {
        if (view.getTag() instanceof Task ) {
            int error = appUser.checkTask((Task) view.getTag());
            if (error != RewardErrorCode.TASK_OK) {
                toastError(error);
                return;
            }
        }
        RewardVideoTask task = (RewardVideoTask) view.getTag();
        if (task != null) {
            FuseAdLoader loader = FuseAdLoader.get(task.adSlot, getActivity());
            if (loader == null) {
                MLogs.d("Wrong adSlot config in task " + task.toString());
                toastError(RewardErrorCode.TASK_UNEXPECTED_ERROR);
                return;
            }
            if(! loader.hasValidCache() ) {
                taskRunningProgressBar.setVisibility(View.VISIBLE);
            }
            loader.loadAd(getActivity(), 2, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    taskRunningProgressBar.setVisibility(View.GONE);
                    ad.show();
                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {
                    loader.preloadAd(getActivity());
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    taskRunningProgressBar.setVisibility(View.GONE);
                    toastError(RewardErrorCode.TASK_AD_NO_FILL);
                }

                @Override
                public void onRewarded(IAdAdapter ad) {
                    appUser.finishTask(task, new RewardTaskListener(view));
                }
            });
        }

    }

    private void toastError(int code) {
        Toast.makeText(getActivity(), RewardErrorCode.getToastMessage(code), Toast.LENGTH_SHORT).show();
    }

    private void toastDone(float payment) {
        if(payment > 0) {
            Toast.makeText(getActivity(), RewardErrorCode.getToastMessage(RewardErrorCode.TASK_OK, payment), Toast.LENGTH_SHORT).show();
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
                onCheckInClick(v);
                break;
            case R.id.invite_task_item:
                onInviteFriendsClick(v);
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
            updateTaskViewItem(mView, (Task)mView.getTag(), false);
            toastDone(payment);
        }

        @Override
        public void onTaskFail(long taskId, ADErrorCode code) {
            toastError(code.getErrCode());
        }

        @Override
        public void onGetAllAvailableTasks(ArrayList<Task> tasks) {

        }

        @Override
        public void onGeneralError(ADErrorCode code) {
            toastError(code.getErrCode());
        }
    }
}
