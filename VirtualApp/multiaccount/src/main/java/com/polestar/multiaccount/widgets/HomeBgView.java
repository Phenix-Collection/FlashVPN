package com.polestar.multiaccount.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.Logs;

import java.util.Timer;

/**
 * Created by yxx on 2016/8/15.
 */
public class HomeBgView extends SurfaceView implements SurfaceHolder.Callback {
    private static final float[] MOVE_SPEED = new float[]{40, 43, 14, 16};
    private SurfaceHolder holder;
    private boolean isRunning;
    private Canvas canvas;
    private Bitmap bg01, bg02, bg03, bg04, bg05, mainBg;
    private Matrix matrix;
    private Paint mPaint;
    private Context mContext;
    private float scaleX, scaleY;
    private int count;
    private int drawRound = 60;
    private int screenHeight, screenWidth;
    private int variator = 1;
    private Handler mHandler;
    private ValueAnimator animator;
    private long animatorTime;
    private DrawerThread drawerThread;
    private boolean isBitmapInit;
    private ImageView coverImg;

    public HomeBgView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public HomeBgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void setCoverImg(ImageView coverImg) {
        this.coverImg = coverImg;
    }

    private void init() {
        holder = getHolder();
        holder.addCallback(this);
//        holder.setFormat(-2);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        matrix = new Matrix();

        screenHeight = DisplayUtils.getScreenHeight(mContext);
        screenWidth = DisplayUtils.getScreenWidth(mContext);

        mainBg = BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_04_min);

        scaleX = DisplayUtils.getScreenWidth(mContext) / (float) mainBg.getWidth();
        scaleY = DisplayUtils.getScreenHeight(mContext) / (float) mainBg.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        mainBg = Bitmap.createBitmap(mainBg, 0, 0, mainBg.getWidth(), mainBg.getHeight(), matrix, true);

        MOVE_SPEED[0] = MOVE_SPEED[0] * scaleY * variator;
        MOVE_SPEED[1] = MOVE_SPEED[1] * scaleY * variator;
        MOVE_SPEED[2] = MOVE_SPEED[2] * scaleY * variator;
        MOVE_SPEED[3] = MOVE_SPEED[3] * scaleY * variator;

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Logs.e("HomeBgView", "surfaceCreated");
        isRunning = true;
        if (drawerThread == null) {
            if (coverImg != null) {
                ViewHelper.setAlpha(coverImg, 1f);
            }
            drawerThread = new DrawerThread();
            drawerThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Logs.e("HomeBgView", "surfaceDestroyed");
        isRunning = false;
        if (drawerThread != null) {
            drawerThread.destroySelf();
            drawerThread = null;
        }

    }

    public void stopSelf() {
        if (drawerThread != null) {
            drawerThread.destroySelf();
            drawerThread = null;
        }
    }

    private void drawBitmapBySpeed(Canvas canvas, Bitmap bitmap, int speed, float value) {
//        matrix.reset();
//        matrix.setScale(scaleX, scaleY);
//        matrix.setTranslate(0, (-speed * value) % screenHeight);
        if (isRunning && canvas != null) {
//            canvas.drawBitmap(bitmap, matrix, mPaint);
            canvas.drawBitmap(bitmap, 0, (-speed * value) % screenHeight, mPaint);
        }

//         matrix.reset();
//            matrix.setScale(scaleX, scaleY);
//        matrix.setTranslate(0, screenHeight - (speed * value) % screenHeight);

        if (isRunning && canvas != null) {
            canvas.drawBitmap(bitmap, 0, screenHeight - (speed * value) % screenHeight, mPaint);
//            canvas.drawBitmap(bitmap, matrix, mPaint);
        }
    }

    private void drawBitmap(Canvas canvas, Bitmap bitmap) {
//        Logs.e("HomeBgView","drawBitmap111111111111");
        if (isRunning && canvas != null) {
//            Logs.e("HomeBgView","drawBitmap22222222222222");
            canvas.drawBitmap(bitmap, 0, 0, mPaint);
        }
    }

    private void createAnimator() {
        float time = (screenHeight / MOVE_SPEED[0]) * (screenHeight / MOVE_SPEED[2]) * (screenHeight / MOVE_SPEED[3]) * screenHeight / MOVE_SPEED[1] * 8;
        ValueAnimator animator = ValueAnimator.ofFloat(0, time);
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration((long) (time * 1000));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!isRunning)
                    return;
                try {
                    Canvas canvas = holder.lockCanvas();
                    float currentValue = (float) valueAnimator.getAnimatedValue();
                    if (canvas != null) {
//                        Logs.e("HomeBgView",currentValue + "");
                        drawBitmap(canvas, mainBg);
                        drawBitmapBySpeed(canvas, bg04, (int) MOVE_SPEED[3], currentValue);
                        drawBitmapBySpeed(canvas, bg03, (int) MOVE_SPEED[2], currentValue);
                        drawBitmapBySpeed(canvas, bg02, (int) MOVE_SPEED[1], currentValue);
                        drawBitmapBySpeed(canvas, bg01, (int) MOVE_SPEED[0], currentValue);
                        if (isRunning) {
                            holder.unlockCanvasAndPost(canvas);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        animator.start();

    }

    private void scaleBitmap() {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        bg01 = Bitmap.createBitmap(bg01, 0, 0, bg01.getWidth(), bg01.getHeight(), matrix, true);
        bg02 = Bitmap.createBitmap(bg02, 0, 0, bg02.getWidth(), bg02.getHeight(), matrix, true);
        bg03 = Bitmap.createBitmap(bg03, 0, 0, bg03.getWidth(), bg03.getHeight(), matrix, true);
        bg04 = Bitmap.createBitmap(bg04, 0, 0, bg04.getWidth(), bg04.getHeight(), matrix, true);
//        mainBg = Bitmap.createBitmap(mainBg, 0, 0, mainBg.getWidth(), mainBg.getHeight(), matrix, true);
    }


    class DrawerThread extends Thread {

        private Handler mHandler;

        DrawerThread() {
            setPriority(Thread.MAX_PRIORITY);
        }

        public void destroySelf() {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(0);
            }
        }

        @Override
        public void run() {
            Looper.prepare();
            if (!isRunning)
                return;
            mHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            Looper mLooper = Looper.myLooper();
                            if (mLooper != null) {
                                mLooper.quit();
                            }
                            break;
                    }
                }
            };

            if (Build.VERSION.SDK_INT == 18) {
                try {
                    Thread.sleep(600);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isRunning)
                                return;
                            AnimatorHelper.fadeOut(coverImg);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                drawBitmap(canvas, mainBg);
                holder.unlockCanvasAndPost(canvas);
            }
            if (!isBitmapInit) {
                isBitmapInit = true;
                bg01 = BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_01_min);
                bg02 = BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_02_min);
                bg03 = BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_03_min);
                bg04 = BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_04_min);
                scaleBitmap();
            }
//            if(animator == null){
            createAnimator();
//            }
            Looper.loop();
        }
    }

}