package com.polestar.superclone.reward;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;

/**
 * Created by guojia on 2019/1/23.
 */

public class RewardCenterFragment extends BaseFragment {
    private View contentView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.reward_center_layout, null);
        return contentView;
    }
}
