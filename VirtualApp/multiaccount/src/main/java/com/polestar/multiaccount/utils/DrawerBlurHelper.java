package com.polestar.multiaccount.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewTreeObserver;

import com.polestar.multiaccount.R;

/**
 * 模糊drawerlayout侧边栏背景
 * Created by yxx on 2016/8/3.
 */
public class DrawerBlurHelper {

    /**
     * 先对图片进行缩放，提高模糊效率
     */
    private static final int SCALE_FACTOR = 6;
    private DrawerLayout drawer;
    /**
     * 需要模糊的背景
     */
    private View bgView;
    /**
     * 在forgroundView上绘制模糊后的图片
     */
    private View fgView;
    /**
     * 侧边栏
     */
    private View navigationView;
    /**
     * 侧边栏宽度
     */
    private int navigationWidth;
    /**
     * 缓存的模糊后的bitmap
     */
    private Bitmap cacheBitmap;
    private Context context;
    /**
     * 侧边栏内容区所占比例
     */
    private float contentPercent = 1f;
    /**
     * 缓存模糊后的bitmap
     */
    private boolean isCacheBitmapRecycled;

    private float slideOffset;

    public DrawerBlurHelper(Context context, DrawerLayout drawer, View bgView, View fgView, View navigationView) {
        this.context = context;
        this.drawer = drawer;
        this.bgView = bgView;
        this.fgView = fgView;
        this.navigationView = navigationView;
    }
    public void setContentPercentage(float contentPercent) {
        this.contentPercent = contentPercent;
    }

    public void blur() {
        navigationWidth = navigationView.getWidth();
        navigationView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int[] location= new int[2];
                navigationView.getLocationInWindow(location);
                float currentX = location[0];
                slideOffset = 1 - Math.abs(currentX / navigationWidth);
                if (slideOffset < 0) {
                    slideOffset = 0;
                }
                float lastOffset = slideOffset;
                if(slideOffset == lastOffset && lastOffset == 1){
//                    recycleCachedBitmap();
                    return true;
                }
                if(slideOffset == lastOffset && lastOffset == 0){
                    recycleCachedBitmap();
                    return true;
                }
                if (isCacheBitmapRecycled || cacheBitmap == null || cacheBitmap.isRecycled()) {
                    createCacheBitmap();
                    isCacheBitmapRecycled = false;
                }
                clipForground((slideOffset - 1 + contentPercent)* navigationWidth / SCALE_FACTOR);
                return true;
            }
        });
    }

    private void recycleCachedBitmap(){
        isCacheBitmapRecycled = true;
        if(cacheBitmap != null){
            cacheBitmap.recycle();
            cacheBitmap = null;
        }
    }

    public void createCacheBitmap() {
        bgView.setBackgroundResource(R.mipmap.main_bg_min);
        Logs.e("createCacheBitmap");
        bgView.destroyDrawingCache();
        if (!bgView.isDrawingCacheEnabled()) {
            bgView.setDrawingCacheEnabled(true);
        }else {
            bgView.buildDrawingCache();
        }
        Bitmap srcBitmap = bgView.getDrawingCache();
        if(srcBitmap == null){
            return;
        }
        cacheBitmap = BitmapUtils.createBlurBitmap(context, srcBitmap, SCALE_FACTOR);
        bgView.destroyDrawingCache();
        bgView.setDrawingCacheEnabled(false);
        bgView.setBackground(null);
    }

    public Bitmap createBitmap() {
        Bitmap bgBitmap;
        bgView.setBackgroundResource(R.mipmap.main_bg_min);
        Logs.e("createCacheBitmap");
        bgView.destroyDrawingCache();
        if (!bgView.isDrawingCacheEnabled()) {
            bgView.setDrawingCacheEnabled(true);
        }else {
            bgView.buildDrawingCache();
        }
        Bitmap srcBitmap = bgView.getDrawingCache();
        if(srcBitmap == null){
            return null;
        }
        bgBitmap = BitmapUtils.createBlurBitmap(context, srcBitmap, SCALE_FACTOR);
        bgView.destroyDrawingCache();
        bgView.setDrawingCacheEnabled(false);
        bgView.setBackground(null);
        return bgBitmap;
    }

    private void clipForground(float slideOffset) {
        if(slideOffset < 0){
            slideOffset = 0;
        }
        if(isCacheBitmapRecycled || cacheBitmap == null || cacheBitmap.isRecycled()){
            return;
        }
        Bitmap overlay = Bitmap.createBitmap(bgView.getMeasuredWidth() / SCALE_FACTOR,bgView.getMeasuredHeight() / SCALE_FACTOR, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        int visibleHeight = bgView.getHeight();

        Rect src = new Rect(0, 0, (int) (slideOffset), visibleHeight);//从view的截图中截取的区域
        RectF dest = new RectF(0, 0, slideOffset, visibleHeight);//设置Drawer背景的区域
        if(isCacheBitmapRecycled || cacheBitmap == null || cacheBitmap.isRecycled()){
            return;
        }
        canvas.drawBitmap(cacheBitmap, src, dest, paint);
        if (Build.VERSION.SDK_INT < 16) {
            fgView.setBackgroundDrawable(new BitmapDrawable(context.getResources(), overlay));
        } else {
            fgView.setBackground(new BitmapDrawable(context.getResources(), overlay));
        }
    }

}
