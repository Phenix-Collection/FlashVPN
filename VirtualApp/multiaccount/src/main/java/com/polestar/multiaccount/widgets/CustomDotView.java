package com.polestar.multiaccount.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.polestar.multiaccount.utils.DisplayUtils;

/**
 * Created by hxx on 8/17/16.
 */
public class CustomDotView extends View {

    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;
    private static final int MSG_DROW_VIEW_ONCE = 0x101;
    private static final int MSG_DROW_VIEW_ALWAYS = 0x102;
    private float startX;
    private float startY;
    private float interval;
    private int orientation;
    private int curCount;
    private int totalCount;
    private long duration;
    private boolean paused;
    private float[] pts;
    private Paint mPaint;
    private float dotWidth;
    private ViewHandler mHandler = new ViewHandler();
    private OnceAnimatorListener mListener;
    private Context mContext;

    public CustomDotView(Context context) {
        super(context);
        init(context);
    }

    public CustomDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // width(dip)
    public void setDotWidth(float width) {
        this.dotWidth = DisplayUtils.dip2px(mContext, width);
    }

    // interval(dip)
    public void setInterval(float interval) {
        this.interval = DisplayUtils.dip2px(mContext, interval);
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        pts = new float[totalCount * 2];
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void init(Context context) {
        mContext = context;
        curCount = 0;
        orientation = VERTICAL;
        totalCount = 15;
        duration = 300;
        interval = DisplayUtils.dip2px(context, 1f);
        dotWidth = DisplayUtils.dip2px(context, 2f);
        paused = false;

        mPaint = new Paint();
        mPaint.setColor(0xFF5A6481);
        mPaint.setStrokeWidth(dotWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        pts = new float[totalCount * 2];
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        float length = (dotWidth + interval) * totalCount - interval;
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            if (orientation == VERTICAL) {
                setMeasuredDimension((int) dotWidth, (int) length);
            } else if (orientation == HORIZONTAL) {
                setMeasuredDimension((int) length, (int) dotWidth);
            }
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            if (orientation == VERTICAL) {
                setMeasuredDimension((int) dotWidth, heightSpecSize);
            } else if (orientation == HORIZONTAL) {
                setMeasuredDimension((int) length, heightSpecSize);
            }
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            if (orientation == VERTICAL) {
                setMeasuredDimension(widthSpecSize, (int) length);
            } else if (orientation == HORIZONTAL) {
                setMeasuredDimension(widthSpecSize, (int) dotWidth);
            }
        }

        startX = 0;
        startY = 0;

        if (orientation == VERTICAL) {
            for (int i = 0; i < totalCount; i++) {
                pts[i * 2] = startX;
                pts[i * 2 + 1] = startY + i * (interval + dotWidth);
            }
        } else if (orientation == HORIZONTAL) {
            for (int i = 0; i < totalCount; i++) {
                pts[i * 2] = startX + i * (interval + dotWidth);
                pts[i * 2 + 1] = startY;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!paused) {
            drawDots(canvas, mPaint);
        }
    }

    private void drawDots(Canvas canvas, Paint p) {
        for (int j = 0; j < this.curCount; j++) {
            canvas.drawPoint(pts[j * 2], pts[j * 2 + 1], p);
        }
    }

    public void play() {
        if (paused) {
            paused = false;
        }
        curCount = 0;
        mHandler.sendEmptyMessage(MSG_DROW_VIEW_ALWAYS);
    }

    public void pause() {
        if (!paused) {
            paused = true;
            invalidate();
        }
    }

    public void playOnce() {
        if (paused) {
            paused = false;
        }
        curCount = 0;
        mHandler.sendEmptyMessage(MSG_DROW_VIEW_ONCE);
    }

    private class ViewHandler extends Handler {

        public ViewHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DROW_VIEW_ONCE:
                    if (curCount >= totalCount) {
                        if (mListener != null) {
                            mListener.onAnimationEnd();
                        }
                        break;
                    }
                    curCount++;
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(MSG_DROW_VIEW_ONCE, duration / totalCount);
                    break;
                case MSG_DROW_VIEW_ALWAYS:
                    if (curCount >= totalCount) {
                        curCount = 0;
                    }
                    curCount++;
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(MSG_DROW_VIEW_ALWAYS, duration / totalCount);
                    break;
            }
        }
    }

    public interface OnceAnimatorListener {
        public void onAnimationEnd();
    }

    public void addListener(OnceAnimatorListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }
}
