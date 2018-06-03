package com.polestar.superclone;


import com.lody.virtual.client.core.IAppApiDelegate;

/**
 * Created by guojia on 2016/12/19.
 */

public class AppApiDelegate implements IAppApiDelegate {

    @Override
    public String getCloneTagedLabel(String label) {
        return label == null? MApp.getApp().getResources().getString(R.string.app_alias_name) :
                MApp.getApp().getResources().getString(R.string.clone_label_tag, label);
    }
}
