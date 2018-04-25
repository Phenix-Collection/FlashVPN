package mochat.multiple.parallel.whatsclone.widgets.locker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.utils.BlurUtils;
import mochat.multiple.parallel.whatsclone.utils.ColorUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by guojia on 2017/7/23.
 */

public class LockerThemeLogic {

    private static final int MSG_RESUME_BG = 0x01;

    private static final int MSG_FADE_IN_BG = 0x02;

    private static final int MSG_BOUNCE_ICON = 0x03;

    private static float TILT_PORTION = 0.25f;

    private static final int mBlurBoundAlphaSpace = 30;

    private static final float[] RESUME_RUNDOWN = {0.1f, 0.15f, 0.15f, 0.3f, 0.2f, 0.1f};

    private static final int RESUME_LENGTH = 6;

    private static float[] BOUNCE_RUNDOWN = {0.25f, 0.75f, 1, 0f, -1.5f, -2, -1, 1};

    private static int BOUNCE_LENGTH = 8,
            BOUNCE_SHIFT_UNIT = 10;

    private final int mBlurBoundSizeSpace;

    private final boolean mIsWindowMode;

    private Context mContext = null;

    private View mView = null;

    private BitmapDrawable mBlurDrawable = null;

    private Paint mGradientPaint = null;

    private View mAppIconView = null;

    private int mBackgroundWidth = 0, mBackgroundHeight = 0;

    private int mBackgroundLeft,
            mBackgroundTop,
            mBackgroundLeftBound,
            mBackgroundRightBound,
            mBackgroundTopBound,
            mBackgroundBottomBound;

    private int mBoundSize = 0;

    private Rect mRectScreen;

    private int mResumeOffsetX,
            mResumeOffsetY,
            mResumeIndex = 0;

    private float mTiltOriginX, mTiltOriginY;

    private int mAlpha = 0;
    private int mMainDarkColor = Color.TRANSPARENT;
    private Paint mMaskPaint = null;

    private boolean mUseBigIcon = false;


    private int mIconBounceIndex = 0;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESUME_BG:
                    if (mResumeIndex == RESUME_LENGTH) {
                        return;
                    }
                    moveBackground((int) (RESUME_RUNDOWN[mResumeIndex] * mResumeOffsetX),
                            (int) (RESUME_RUNDOWN[mResumeIndex] * mResumeOffsetY));
                    ++mResumeIndex;
                    mHandler.sendEmptyMessageDelayed(MSG_RESUME_BG, 10);
                    break;
                case MSG_FADE_IN_BG:
                    if (mAlpha < 255) {
                        mAlpha += 15;
                        mHandler.sendEmptyMessageDelayed(MSG_FADE_IN_BG, 10);
                    } else {
                        mAlpha = 255;
                    }
                    mView.postInvalidate();
                    break;
                case MSG_BOUNCE_ICON:
                    if (mAppIconView == null) {
                        break;
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 11) {
                        if (mIconBounceIndex == BOUNCE_LENGTH) {
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)mAppIconView.getLayoutParams();
                            int left = 0;
                            if (params != null) {
                                left = params.leftMargin;
                            }
                            callViewSetLeft(mAppIconView, left);
                            mAppIconView = null;
                            return;
                        }

                        callViewSetLeft(mAppIconView,
                                (int) (mAppIconView.getLeft()
                                        + BOUNCE_RUNDOWN[mIconBounceIndex++] * BOUNCE_SHIFT_UNIT));

                        if (mIsWindowMode) {
                            mHandler.sendEmptyMessageDelayed(MSG_BOUNCE_ICON, 5);
                            mView.postInvalidate();
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_BOUNCE_ICON, 20);
                        }
                    }
                    break;
            }
        }
    };

    public LockerThemeLogic(Context context, View view) {
        mContext = context;
        mView = view;
        mBlurBoundSizeSpace = (int) mContext.getResources().getDimension(R.dimen.blur_bound_size);
        mIsWindowMode = true;
    }

    public void setDimension() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        if (metrics.widthPixels < metrics.heightPixels) {
            mBackgroundHeight = metrics.heightPixels;
            mBackgroundWidth = metrics.widthPixels;
        } else {
            mBackgroundWidth = metrics.heightPixels;
            mBackgroundHeight = metrics.widthPixels;
        }

        calculateBounds();

    }

    public void setUseBigIcon(boolean enabled, boolean darkerMask) {
        mUseBigIcon = enabled;
        calculateBounds();
        if (enabled) {
            LinearGradient gradient;
            if (!darkerMask) {
                gradient = new LinearGradient(0, 0, 0, 1, 0x99000000, 0x33000000, Shader.TileMode.CLAMP);
            } else {
                gradient = new LinearGradient(0, 0, 0, 1, 0xbb000000, 0x55000000, Shader.TileMode.CLAMP);
            }
            mMaskPaint = new Paint();
            mMaskPaint.setDither(true);
            mMaskPaint.setShader(gradient);
        } else {
            mMaskPaint = null;
        }
    }

    private void calculateBounds() {
        int right, bottom;
        if (mUseBigIcon) {
            mBoundSize = (int) (mBackgroundHeight * 1.2f * 1.15f);
            mBackgroundLeft = (mBackgroundWidth - mBoundSize) / 2;
            mBackgroundTop = (mBackgroundHeight - mBoundSize) / 2;
            right = mBackgroundLeft + mBoundSize;
            bottom = mBackgroundTop + mBoundSize;
        } else {
            int paddingLeft = (mBlurBoundAlphaSpace / 2 * mBackgroundHeight) / (mBlurBoundAlphaSpace + mBlurBoundSizeSpace);
            mBoundSize = (int) (mBackgroundHeight * 1.2f);
            mBackgroundLeft = -paddingLeft;
            mBackgroundTop = -(mBoundSize - mBackgroundHeight * 5 / 7 - paddingLeft);

            right = -paddingLeft + mBoundSize;
            bottom = mBackgroundHeight * 5 / 7 + paddingLeft;
        }

        mBackgroundLeftBound = (int)(mBackgroundLeft - mBackgroundWidth * TILT_PORTION);
        mBackgroundRightBound = (int)(mBackgroundLeft + mBackgroundWidth * TILT_PORTION);
        mBackgroundTopBound = (int)(mBackgroundTop - mBackgroundHeight * TILT_PORTION);
        mBackgroundBottomBound = (int)(mBackgroundTop + mBackgroundHeight * TILT_PORTION);
        mRectScreen = new Rect(mBackgroundLeft, mBackgroundTop, right, bottom);
    }


    public void setBackground(String packageName, Drawable icon) {
        createGradientBackground(packageName, icon);
        createBlurDarwable(icon);
    }

    public void draw(Canvas canvas) {
        if (mGradientPaint != null) {
            canvas.drawPaint(mGradientPaint);
        }

        if (mBlurDrawable != null) {
            mBlurDrawable.setBounds(mRectScreen);
            mBlurDrawable.setAlpha(mAlpha);
            mBlurDrawable.draw(canvas);
        }

        if (mMaskPaint != null) {
            canvas.drawPaint(mMaskPaint);
        }
    }

    public void onHide() {
        mHandler.removeMessages(MSG_FADE_IN_BG);
        mHandler.removeMessages(MSG_RESUME_BG);
        mHandler.removeMessages(MSG_BOUNCE_ICON);
        mGradientPaint = null;
        releaseBlurDrawable();
        mAppIconView = null;
    }

    private static final boolean sEnableBackgroundAnim = false;

    public void onTouch(MotionEvent ev) {
        if(sEnableBackgroundAnim) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                setTiltOrigin(ev.getX(), ev.getY());
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                tiltBackground(ev.getX(), ev.getY());
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                resumeBackgroundPosition();
            }
        }
    }

    public void onIncorrectPassword(View appIconView) {
        mIconBounceIndex = 0;
        mAppIconView = appIconView;
        mHandler.sendEmptyMessage(MSG_BOUNCE_ICON);
    }

    private synchronized void createGradientBackground(String pkg, Drawable icon) {
        int mainColor = ColorUtils.getIconMainColor(pkg, icon);
        mMainDarkColor = ColorUtils.getDarkerColor(mainColor);
        RadialGradient gradient = new RadialGradient(mBackgroundWidth, mBackgroundHeight, mBackgroundWidth, mainColor, mMainDarkColor, Shader.TileMode.CLAMP);

        mGradientPaint = new Paint();
        mGradientPaint.setDither(true);
        mGradientPaint.setShader(gradient);
        mView.post(new Runnable() {

            @Override
            public void run() {
                mView.invalidate();
            }
        });
    }

    private synchronized void createBlurDarwable(Drawable icon) {

        final Bitmap blur;
        try {
            if (mUseBigIcon) {
                blur = BlurUtils.boxBlurFilter(icon, mBlurBoundAlphaSpace, mBlurBoundSizeSpace, mMainDarkColor, 7, 7, 4);
            } else {
                blur = BlurUtils.boxBlurFilter(icon, mBlurBoundAlphaSpace, mBlurBoundSizeSpace, mMainDarkColor);
            }

            mView.post(new Runnable() {

                @Override
                public void run() {
                    releaseBlurDrawable();
                    mBlurDrawable = new BitmapDrawable(blur);
                    mAlpha = 0;
                    mHandler.sendEmptyMessageDelayed(MSG_FADE_IN_BG, 10);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setTiltOrigin(float x, float y) {
        mTiltOriginX = x;
        mTiltOriginY = y;
    }

    private void tiltBackground(float offsetX, float offsetY) {
        int x = -(int) ((offsetX - mTiltOriginX) * TILT_PORTION);
        int y = -(int) ((offsetY - mTiltOriginY) * TILT_PORTION);
        moveBackground(x, y);
        mTiltOriginX = offsetX;
        mTiltOriginY = offsetY;
    }

    private boolean moveBackground(int offsetX, int offsetY) {
        boolean ret = true;
        mRectScreen.left += offsetX;
        mRectScreen.right += offsetX;
        mRectScreen.top += offsetY;
        mRectScreen.bottom += offsetY;
        if (mRectScreen.left < mBackgroundLeftBound) {
            mRectScreen.left = mBackgroundLeftBound;
            mRectScreen.right = mRectScreen.left + mBoundSize;
            ret = false;
        } else if (mRectScreen.left > mBackgroundRightBound) {
            mRectScreen.left = mBackgroundRightBound;
            mRectScreen.right = mRectScreen.left + mBoundSize;
            ret = false;
        }
        if (mRectScreen.top < mBackgroundTopBound) {
            mRectScreen.top = mBackgroundTopBound;
            mRectScreen.bottom = mRectScreen.top + mBoundSize;
            ret = false;
        } else if (mRectScreen.top > mBackgroundBottomBound) {
            mRectScreen.top = mBackgroundBottomBound;
            mRectScreen.bottom = mRectScreen.top + mBoundSize;
            ret = false;
        }
        mView.post(new Runnable() {
            @Override
            public void run() {
                mView.invalidate();
            }
        });
        return ret;
    }

    private void resumeBackgroundPosition() {
        mResumeOffsetX = mBackgroundLeft - mRectScreen.left;
        mResumeOffsetY = mBackgroundTop - mRectScreen.top;
        mResumeIndex = 0;
        mHandler.sendEmptyMessage(MSG_RESUME_BG);
    }

    private void callViewSetLeft(View view, int left) {
        try {
            Method methodSetLeft = View.class.getDeclaredMethod("setLeft", Integer.TYPE);
            methodSetLeft.invoke(view, left);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isBlurIconInited() {
        return mBlurDrawable != null;
    }

    private void releaseBlurDrawable() {
        if (mBlurDrawable != null) {
            if (mBlurDrawable.getBitmap() != null) {
                mBlurDrawable.getBitmap().recycle();
            }
            mBlurDrawable = null;
        }
    }
}
