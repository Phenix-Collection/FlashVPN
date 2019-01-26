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
        points.setText(String.format("%.1f%s",appUser.getMyBalance() , getActivity().getString(R.string.reward_center)));
    }

    private void updateBasicTasks(){
        bindTaskViewItem(inviteItemView, appUser.getInviteTask());
        bindTaskViewItem(checkinItemView, appUser.getCheckInTask());
        bindTaskViewItem(videoItemView, appUser.getVideoTask());
    }

    private void bindTaskViewItem(View view, Task task){
        switch (view.getId()) {
            case R.id.invite_task_item:
                break;
            case R.id.checkin_task_item:
                break;
            case R.id.video_task_item:
                break;
        }
    }

    public void onStoreClick(View view){
        MLogs.d("onStoreClick");

    }
}
