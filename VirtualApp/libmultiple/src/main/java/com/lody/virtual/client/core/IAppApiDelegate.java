package com.lody.virtual.client.core;

import android.graphics.Bitmap;

/**
 * Created by guojia on 2016/12/19.
 */

public interface IAppApiDelegate {

    Bitmap createCloneTagedBitmap(String pkg, Bitmap icon, int userId);

    String getCloneTagedLabel(String label);
}
