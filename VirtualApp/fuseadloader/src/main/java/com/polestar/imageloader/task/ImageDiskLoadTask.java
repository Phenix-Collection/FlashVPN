
package com.polestar.imageloader.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.polestar.imageloader.DiskCacheUtils;
import com.polestar.imageloader.ImageLoadTask;
import com.polestar.imageloader.ProgressRecorder;


public class ImageDiskLoadTask extends ImageLoadTask {

    private Context context;

    public ImageDiskLoadTask(Handler handler, String url, Context context) {
        super(handler, url);
        this.context = context;
    }

    @Override
    protected Bitmap load(String url) {
        ProgressRecorder.getInstance().setProgress(url, 1.0f);
        return DiskCacheUtils.getBitmap(context, url);
    }

}
