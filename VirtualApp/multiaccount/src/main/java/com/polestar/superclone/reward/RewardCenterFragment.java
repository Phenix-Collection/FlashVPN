package com.polestar.superclone.reward;

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

import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.database.DatabaseApi;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.network.datamodels.Task;

/**
 * Created by guojia on 2019/1/23.
 */

public class RewardCenterFragment extends BaseFragment implements AppUser.IUserUpdateListener, View.OnClickListener{
    private View contentView;
    private View inviteItemView;
    private View checkinItemView;
    private View videoItemView;
    private View userInfoView;
    private AppUser appUser;
    private ProgressBar loadingProgressBar;
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

    private void initView() {
        userInfoView = contentView.findViewById(R.id.reward_user_info_layout);
        View store = userInfoView.findViewById(R.id.store_button);
        store.setOnClickListener(this);
        inviteItemView = contentView.findViewById(R.id.invite_task_item);
        checkinItemView = contentView.findViewById(R.id.checkin_task_item);
        videoItemView = contentView.findViewById(R.id.video_task_item);
        loadFailLayout = contentView.findViewById(R.id.loading_fail_layout);
        loadingProgressBar = contentView.findViewById(R.id.loading_layout);
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
            updateBasicTasks();
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
        TextView points = userInfoView.findViewById(R.id.user_balance_txt);
        points.setText(String.format(getString(R.string.you_have_coins),appUser.getMyBalance() , getActivity().getString(R.string.coin_unit)));
    }

    private void updateBasicTasks(){
        bindTaskViewItem(inviteItemView, appUser.getInviteTask());
        bindTaskViewItem(checkinItemView, appUser.getCheckInTask());
        bindTaskViewItem(videoItemView, appUser.getVideoTask());
    }

    private void bindTaskViewItem(View view, Task task){
        TextView title = view.findViewById(R.id.task_title);
        TextView description = view.findViewById(R.id.task_description);
        IconFontTextView icon = view.findViewById(R.id.task_icon);
        TextView reward = view.findViewById(R.id.task_reward);
        reward.setText("+" + String.format("%.0f", task.mPayout));
        title.setText(task.mTitle);
        description.setText(task.mDescription);
        switch (task.mTaskType) {
            case Task.TASK_TYPE_CHECKIN_TASK:
                icon.setText((R.string.iconfont_checkin));
                break;
            case Task.TASK_TYPE_SHARE_TASK:
                icon.setText((R.string.iconfont_invite));
                break;
            case Task.TASK_TYPE_REWARDVIDEO_TASK:
                icon.setText((R.string.iconfont_video));
                break;
        }
    }

    public void onStoreClick(View view){
        MLogs.d("onStoreClick");

    }

    public void onRetryClick(View view) {
        loadedLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadFailLayout.setVisibility(View.GONE);
        mainHandler.sendEmptyMessageDelayed(MSG_LOAD_TIMEOUT, LOAD_TIMEOUT);
        appUser.forceRefreshData();
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
        }
    }
}
