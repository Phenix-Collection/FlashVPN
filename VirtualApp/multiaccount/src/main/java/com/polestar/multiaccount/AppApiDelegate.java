package com.polestar.multiaccount;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.lody.virtual.client.core.IAppApiDelegate;
import com.polestar.multiaccount.utils.BitmapUtils;

/**
 * Created by guojia on 2016/12/19.
 */

public class AppApiDelegate implements IAppApiDelegate {

    @Override
    public Bitmap createCloneTagedBitmap(String pkg, Bitmap icon) {
        if (pkg == null && icon == null) {
            return null;
        }
        if (icon == null) {
            PackageManager pm = MApp.getApp().getPackageManager();
            Drawable drawable = null;
            try {
                drawable = pm.getApplicationIcon(pkg);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (drawable != null) {
                return BitmapUtils.createCustomIcon(MApp.getApp(), drawable);
            }
        } else {
            return BitmapUtils.createCustomIcon(MApp.getApp(), new BitmapDrawable(icon));
        }

        return null;
    }
}
