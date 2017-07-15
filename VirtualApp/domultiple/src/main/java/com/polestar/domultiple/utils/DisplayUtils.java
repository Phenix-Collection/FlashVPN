package com.polestar.domultiple.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by yxx on 2016/7/14.
 */
public class DisplayUtils {
    private static WindowManager mWindowManager;

    public static int dip2px(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int px2dip(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5F);
    }

    public static int getScreenWidth(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;
        return width;
    }
    public static int[] getScreenSize(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(metric);
        int[] screenSize = new int[2];
        screenSize[0] = metric.widthPixels;
        screenSize[1] = metric.heightPixels;
        return screenSize;
    }
    public static int getScreenHeight(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager(context).getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels;
        return height;
    }

    public static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 在onCreate之后调用，否则返回值为0
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity){
        Rect rectangle= new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
