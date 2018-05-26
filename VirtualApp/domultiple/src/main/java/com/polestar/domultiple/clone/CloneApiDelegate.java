package com.polestar.domultiple.clone;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lody.virtual.client.core.IAppApiDelegate;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.utils.CommonUtils;

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
