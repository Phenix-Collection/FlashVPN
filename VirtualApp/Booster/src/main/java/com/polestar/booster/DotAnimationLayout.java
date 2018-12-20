package com.polestar.booster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DotAnimationLayout extends View {

    private static final String TAG = DotAnimationLayout.class.getSimpleName();

    private Context mContext;
    private ValueAnimator mAnimator;

    private Random mRandom;
    private Paint mPaint;
    private float mLastAnimatedValue;
    private List<Nebula> mNebulaList;
    private Bitmap mBitmap;
    private Matrix mMatrix;

    public DotAnimationLayout(Context context) {
        super(context);

        init(context);
    }

    public DotAnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mNebulaList = new ArrayList<>();
        mRandom = new Random();
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dot_icon);
        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAnimator != null && (mAnimator.isRunning() || mAnimator.isStarted())) {
            mAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimator != null && mAnimator.isStarted()) {
            float value = (float) mAnimator.getAnimatedValue();
            if (value < mLastAnimatedValue) {//new animator start
                mLastAnimatedValue = 0;
                mNebulaList.clear();
            }

            if (value < 0.8) {//produce Nebula
                int produceNebulaCount = mRandom.nextInt(2);
                for (int i = 0; i < produceNebulaCount; i++) {
                    Nebula nebula = newNebula(value);
                    mNebulaList.add(nebula);
                }
            }

            //draw
            for (Nebula nebula : mNebulaList) {
                mPaint.setAlpha(nebula.alpha);
//                float x = nebula.accelerateX*nebula.valueX*value+nebula.srcX;
//                float y = nebula.accelerateY*nebula.valueY*value+nebula.srcY;
                float x = (nebula.destX * (value - nebula.aninmateValue) + (nebula.srcX * (1 - value))) / (1 - nebula.aninmateValue);
                float y = (nebula.destY * (value - nebula.aninmateValue) + (nebula.srcY * (1 - value))) / (1 - nebula.aninmateValue);
                float deltaX = Math.abs(x - nebula.destX);
                float deltaY = Math.abs(y - nebula.destY);
                float distance = (float) Math.sqrt(deltaX * deltaX + (deltaY * deltaY));
                if (distance > 2 * mBitmap.getWidth()) {
                    mMatrix.setTranslate(x, y);
                    mMatrix.preScale(nebula.scale, nebula.scale);
//                    mMatrix.preRotate(nebula.rotate);
                    canvas.drawBitmap(mBitmap, mMatrix, mPaint);
                }
            }

            mLastAnimatedValue = value;
        }
    }

    public ValueAnimator createNebulaAnimator(long duration) {
        mAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(duration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mNebulaList.clear();
                invalidate();
            }
        });
        return mAnimator;
    }

    private Nebula newNebula(float animateValue) {
        Nebula nebula = new Nebula();
        nebula.alpha = mRandom.nextInt(128) + 128;
        nebula.valueX = getWidth() / 2 * obtainSpeed();//
        nebula.valueY = getHeight() / 2 * obtainSpeed();//
        nebula.destX = getWidth() / 2;
        nebula.destY = getHeight() / 2;
        nebula.srcX = nebula.destX - nebula.valueX;
        nebula.srcY = nebula.destY - nebula.valueY;
        nebula.scale = mRandom.nextFloat() / 2 + 0.5f;
//        nebula.rotate = mRandom.nextFloat() * 360;
        nebula.accelerateX = mRandom.nextFloat() / 5 + 1;
        nebula.accelerateY = mRandom.nextFloat() / 5 + 1;
        nebula.aninmateValue = animateValue;
        return nebula;
    }

    private float obtainSpeed() {
        float speed = mRandom.nextFloat() - 0.5f;
        while (speed == 0) {
            speed = mRandom.nextFloat() - 0.5f;
        }
        return 2 * speed;
    }

    class Nebula {
        int alpha;
        float srcX;
        float srcY;
        float destX;
        float destY;
        float valueX;
        float valueY;
        float accelerateX;
        float accelerateY;
        float aninmateValue;
        float scale;
//        float rotate;
    }
}
