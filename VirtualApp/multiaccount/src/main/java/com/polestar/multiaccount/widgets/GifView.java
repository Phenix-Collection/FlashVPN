package com.polestar.multiaccount.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.polestar.multiaccount.utils.MLogs;

/**
 * Created by yxx on 2016/8/8.
 */

public class GifView extends View {
    private static final int DEFAULT_MOVIE_VIEW_DURATION = 1000;
    private int mMovieResourceId;
    private Movie movie;
    private long mMovieStart;
    private int mCurrentAnimationTime;
    private float mLeft;
    private float mTop;
    private float mScale;
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;
    private volatile boolean mPaused = true;
    private boolean mVisible;
    private boolean playOnce;

    private long playDuration = -1;

    public GifView(Context context) {
        this(context, (AttributeSet) null);
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mVisible = true;
        if (Build.VERSION.SDK_INT >= 11) {
            this.setLayerType(1, (Paint) null);
        }
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mVisible = true;
        this.setViewAttributes(context, attrs, defStyle);
    }

    private void setViewAttributes(Context context, AttributeSet attrs, int defStyle) {
        if (Build.VERSION.SDK_INT >= 11) {
            this.setLayerType(1, (Paint) null);
        }
    }

    public void setGifResource(int movieResourceId) {
        this.mMovieResourceId = movieResourceId;
        this.movie = Movie.decodeStream(this.getResources().openRawResource(this.mMovieResourceId));
        this.requestLayout();
    }

    public int getGifResource() {
        return this.mMovieResourceId;
    }

    public void play() {
        if (this.mPaused) {
            this.mPaused = false;
            this.mMovieStart = SystemClock.uptimeMillis() - (long) this.mCurrentAnimationTime;
            this.invalidate();
        }
        playDuration = -1;
    }

    public void playOnce() {
        mCurrentAnimationTime = 0;
        playOnce = true;
        playDuration = this.movie.duration();
        if(playDuration == 0) {
            playDuration = 1000;
        }
        play();
    }

    public void pause() {
        if (!this.mPaused) {
            this.mPaused = true;
            this.invalidate();
        }

    }

    public boolean isPaused() {
        return this.mPaused;
    }

    public boolean isPlaying() {
        return !this.mPaused;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.movie != null) {
            int movieWidth = this.movie.width();
            int movieHeight = this.movie.height();
            float scaleH = 1.0F;
            int measureModeWidth = MeasureSpec.getMode(widthMeasureSpec);
            if (measureModeWidth != 0) {
                int scaleW = MeasureSpec.getSize(widthMeasureSpec);
                scaleH = (float) movieWidth / (float) scaleW;
            }

            float scaleW1 = 1.0F;
            int measureModeHeight = MeasureSpec.getMode(heightMeasureSpec);
            if (measureModeHeight != 0) {
                int maximumHeight = MeasureSpec.getSize(heightMeasureSpec);
                scaleW1 = (float) movieHeight / (float) maximumHeight;
            }

            this.mScale = 1.0F / Math.max(scaleH, scaleW1);
            this.mMeasuredMovieWidth = (int) ((float) movieWidth * this.mScale);
            this.mMeasuredMovieHeight = (int) ((float) movieHeight * this.mScale);
            this.setMeasuredDimension(this.mMeasuredMovieWidth, this.mMeasuredMovieHeight);
        } else {
            this.setMeasuredDimension(this.getSuggestedMinimumWidth(), this.getSuggestedMinimumHeight());
        }

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mLeft = (float) (this.getWidth() - this.mMeasuredMovieWidth) / 2.0F;
        this.mTop = (float) (this.getHeight() - this.mMeasuredMovieHeight) / 2.0F;
        this.mVisible = this.getVisibility() == View.VISIBLE;
    }

    protected void onDraw(Canvas canvas) {
        if (this.movie != null) {
            if (!this.mPaused) {
                this.updateAnimationTime();
                this.drawMovieFrame(canvas);
                this.invalidateView();
            } else {
                this.drawMovieFrame(canvas);
            }
        }

    }

    private void invalidateView() {
        if (this.mVisible) {
            if (Build.VERSION.SDK_INT >= 16) {
                this.postInvalidateOnAnimation();
            } else {
                this.invalidate();
            }
        }

    }

    private void updateAnimationTime() {
        long now = SystemClock.uptimeMillis();
        if (this.mMovieStart == 0L) {
            this.mMovieStart = now;
        }

        int dur = this.movie.duration();
        if (dur == 0) {
            dur = 1000;
        }

        this.mCurrentAnimationTime = (int) ((now - this.mMovieStart) % (long) dur);
        if (playDuration != -1 && (now - this.mMovieStart >= playDuration)) {
            mCurrentAnimationTime = dur;
            pause();
        }
    }

    private void drawMovieFrame(Canvas canvas) {
        this.movie.setTime(this.mCurrentAnimationTime);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(this.mScale, this.mScale);
        this.movie.draw(canvas, this.mLeft / this.mScale, this.mTop / this.mScale);
        canvas.restore();
    }

    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        this.mVisible = screenState == 1;
        this.invalidateView();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.mVisible = visibility == View.VISIBLE;
        this.invalidateView();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mVisible = visibility == View.VISIBLE;
        this.invalidateView();
    }

    public void play(long duration) {
        if (this.mPaused) {
            this.mPaused = false;
            this.mMovieStart = SystemClock.uptimeMillis() - (long) this.mCurrentAnimationTime;
            this.invalidate();
        }
        long movieDuration = this.movie.duration() == 0 ? 1000 : this.movie.duration();
        MLogs.d("movie duration " + movieDuration);
        if (duration < movieDuration) {
            playDuration = movieDuration;
        } else {
            playDuration = ((duration + movieDuration) / movieDuration) * movieDuration;
        }
        MLogs.d("playDuration " + playDuration);
    }
}
