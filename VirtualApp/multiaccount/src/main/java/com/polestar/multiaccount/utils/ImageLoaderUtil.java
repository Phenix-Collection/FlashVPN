package com.polestar.multiaccount.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created by yxx on 2016/8/10.
 */
public class ImageLoaderUtil {

    public static void init(Context context){
        if(ImageLoader.getInstance().isInited())
            return;
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(context);
        ImageLoader.getInstance().init(configuration);
    }

    /**
     * EXACTLY :图像将完全按比例缩小的目标大小
     * <p/>
     * EXACTLY_STRETCHED:图片会缩放到目标大小完全
     * <p/>
     * IN_SAMPLE_INT:图像将被二次采样的整数倍
     * <p/>
     * IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
     * <p/>
     * NONE:图片不会调整
     * 默认初始化ImageLoader参数
     *
     * @param drawableId
     *
     * @return
     */
    public static DisplayImageOptions newOptionInstance(int drawableId) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(drawableId)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)    //ImageScaleType.EXACTLY
                .showImageOnFail(drawableId)
                .showImageForEmptyUri(drawableId)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheOnDisk(true)
                .considerExifParams(true).build();
        return options;
    }

}
