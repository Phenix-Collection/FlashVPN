package com.polestar.domultiple.clone;

import com.polestar.clone.client.core.IAppApiDelegate;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;

/**
 * Created by PolestarApp on 2017/7/16.
 */

public class CloneApiDelegate implements IAppApiDelegate {


    @Override
    public String getCloneTagedLabel(String label) {
        return label == null? PolestarApp.getApp().getResources().getString(R.string.app_name) :
                PolestarApp.getApp().getResources().getString(R.string.clone_label_tag, label);
    }
}
