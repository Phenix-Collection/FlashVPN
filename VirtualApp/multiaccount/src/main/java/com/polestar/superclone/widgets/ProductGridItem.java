package com.polestar.superclone.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.polestar.superclone.reward.AssetHelper;

/**
 * Created by PolestarApp on 2017/7/17.
 */

public class ProductGridItem {
    public CharSequence name;
    public String description;
    public float price;
    public Drawable icon;
    public String iconUrl;

    public void loadIcon(Context context) {
        //目前的实现就是从asset里面去load
        Drawable ret = AssetHelper.getDrawable(context, iconUrl);
        if (ret != null) {
            icon = ret;
        }
    }
}
