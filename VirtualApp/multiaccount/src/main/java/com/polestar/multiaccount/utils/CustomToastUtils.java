package com.polestar.multiaccount.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.polestar.multiaccount.widgets.CustomToast;

/**
 * Created by yxx on 2016/7/14.
 */
public class CustomToastUtils {
    private static CustomToast toast;

    public static void initToast(Context context){
        if (null == toast) {
            toast = new CustomToast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
    }

    /**
     * 居中显示，图片上 文字下
     * @param context
     * @param msg
     * @param imgId
     */
    public static void showImageWithMsg(Context context, String msg, int imgId){
        initToast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.showImageWithMsg(imgId,msg);
    }

}
