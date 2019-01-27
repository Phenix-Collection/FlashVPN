package com.polestar.superclone.reward;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.widgets.IconFontTextView;
import com.polestar.task.network.datamodels.Task;

/**
 * Created by guojia on 2019/1/23.
 */

public class RewardCenterFragment extends BaseFragment {
    private View contentView;
    private View inviteItemView;
    private View checkinItemView;
    private View videoItemView;
    private View userInfoView;
    private AppUser appUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.reward_center_layout, null);
        initView();
        initData();
        return contentView;
    }

    private void initView() {
        userInfoView = contentView.findViewById(R.id.reward_user_info_layout);
        View store = userInfoView.findViewById(R.id.store_button);
        store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStoreClick(v);
            }
        });
        inviteItemView = contentView.findViewById(R.id.invite_task_item);
        checkinItemView = contentView.findViewById(R.id.checkin_task_item);
        videoItemView = contentView.findViewById(R.id.video_task_item);
    }

    private void initData() {
        appUser = AppUser.getInstance();
        updateUserInfo();
        updateBasicTasks();

    }

    private void updateUserInfo() {
        TextView points = userInfoView.findViewById(R.id.user_balance_txt);
        points.setText(String.format("You have %.0f%s",appUser.getMyBalance() , getActivity().getString(R.string.coin_unit)));
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
}
