package com.polestar.multiaccount.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.Logs;

/**
 * Created by yxx on 2016/8/15.
 */
public class AppIconBgView extends View {

    private static final int STATE_IDLE = 1;
    private static final int STATE_ANIMATION = 2;
    private Paint mPaint;
    private Context mContext;
    private int degree;
    private Bitmap iconBg1, iconBg2, iconBg3,iconIdle;
    private Matrix matrix;
    private float bg1Scale, bg2Scale, bg3Scale,bgIdleScale;
    private int contentWidth;
    private int extraWidth;
    private int contentPadding;
    private int bg1Width;
    private int currentState = STATE_IDLE;
    private int minPadding;

    public AppIconBgView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public AppIconBgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(DisplayUtils.dip2px(getContext(), 1));
        matrix = new Matrix();
        extraWidth = DisplayUtils.dip2px(mContext,5);
    }

    public void startAnim() {
        if(currentState == STATE_ANIMATION){
            return;
        }
        currentState = STATE_ANIMATION;
        setVisibility(View.VISIBLE);
        final ValueAnimator animator = ValueAnimator.ofInt(0, 360);
        animator.setDuration(8000);
        animator.setRepeatCount(-1);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int currentValue = (int) valueAnimator.getAnimatedValue();
                if(currentState == STATE_IDLE){
                    degree = 0;
                    animator.cancel();
                    return;
                }
                degree = currentValue;
                invalidate();
            }
        });
        final ValueAnimator scaleAnim = ValueAnimator.ofInt(extraWidth + minPadding, minPadding);
        scaleAnim.setDuration(300);
        scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int currentValue = (int) valueAnimator.getAnimatedValue();
                contentPadding = currentValue;
                invalidate();
                if ( minPadding== currentValue && AppIconBgView.this.getVisibility() == View.VISIBLE) {
                    animator.start();
                }
            }
        });
        scaleAnim.start();
    }

    public void reset(){
        currentState = STATE_IDLE;
        contentPadding = extraWidth + minPadding;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (iconBg1 == null) {
            iconBg1 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_bg1)).getBitmap();
        }
        if (iconBg2 == null) {
            iconBg2 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_bg2)).getBitmap();
            minPadding = (int) ((iconBg1.getWidth() / (float)iconBg2.getWidth() - 1) * getWidth() / 2);
            contentPadding = minPadding + extraWidth;
        }
        contentWidth = getWidth() - contentPadding * 2;

        if (iconBg3 == null) {
            iconBg3 = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_bg3)).getBitmap();
        }
        bg1Width = contentWidth * iconBg1.getWidth() / iconBg2.getWidth();

        if(currentState == STATE_IDLE){
            if(iconIdle == null){
                iconIdle = ((BitmapDrawable) getResources().getDrawable(R.mipmap.app_bg)).getBitmap();
            }
            bgIdleScale = bg1Width / (float)iconIdle.getWidth();
            matrix.reset();
            matrix.setScale(bgIdleScale, bgIdleScale);
            matrix.postTranslate((getWidth() - bg1Width) / 2,(getWidth() - bg1Width) / 2);
            canvas.drawBitmap(iconIdle, matrix, mPaint);
        }else{
            bg2Scale = contentWidth / (float) iconBg2.getWidth();
            bg3Scale = contentWidth / (float) iconBg3.getWidth();

            bg1Scale = bg1Width / (float) iconBg1.getWidth();

            matrix.reset();
            matrix.setScale(bg1Scale, bg1Scale);
            matrix.postTranslate((getWidth() - bg1Width) / 2,(getWidth() - bg1Width) / 2);
            matrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
            canvas.drawBitmap(iconBg1, matrix, mPaint);

            matrix.reset();
            matrix.setScale(bg2Scale, bg2Scale);
            matrix.postTranslate(contentPadding,contentPadding);
            matrix.postRotate(-degree, getWidth() / 2, getHeight() / 2);
            canvas.drawBitmap(iconBg2, matrix, mPaint);

            matrix.reset();
            matrix.setScale(bg3Scale, bg3Scale);
            matrix.postTranslate(contentPadding,contentPadding);
            matrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
            canvas.drawBitmap(iconBg3, matrix, mPaint);
        }
    }

}
