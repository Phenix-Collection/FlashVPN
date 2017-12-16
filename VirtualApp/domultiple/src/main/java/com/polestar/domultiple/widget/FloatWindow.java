package com.polestar.domultiple.widget;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.polestar.domultiple.utils.CommonUtils;

/**
 * Created by PolestarApp on 2017/1/3.
 */

public class FloatWindow {
    private final static String TAG = "FloatWindow";

    protected Context mContext;
    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams mWindowAttributes;
    private float mXDownInScreen;
    private float mYDownInScreen;
    private float mXInView;
    private float mYInView;
    private float mXInScreen;
    private float mYInScreen;

    protected boolean mIsVisible = false;
    protected View mViewRoot;
    protected OnClickListener mOnClickListener;
    protected OnLongClickListener mOnLongClickListener;
    protected OnBackPressedListener mOnBackPressedListener;

    protected boolean mIsFloating;

    private boolean mIsMoved;
    private boolean mIsLongClicked;
    private final int LONG_CLICK_INTERVAL = 500;
    private final int TOUCH_OFFSET_THRESHOLD = 10;
    private Handler mLongClickHandler = new Handler(Looper.getMainLooper());
    private Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            mIsLongClicked = true;
            if (mOnLongClickListener != null) {
                mOnLongClickListener.onLongClick(mContext);
            }
        }
    };

    public interface OnClickListener {
        public void onClick(Context context, int x, int y, int width, int height);
    }

    public interface OnLongClickListener {
        public void onLongClick(Context context);
    }

    public interface OnBackPressedListener {
        public void onBackPressed();
    }

    public FloatWindow(Context context) {
        mContext = context;
        //mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        initWindowAttributes();
    }

    public void setAttributes(WindowManager.LayoutParams params) {
        this.mWindowAttributes = params;
    }

    public WindowManager.LayoutParams getAttributes() {
        return mWindowAttributes;
    }

    private void initWindowAttributes() {
        mWindowAttributes = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            mWindowAttributes.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !CommonUtils.isSamsungDevice()) {
            mWindowAttributes.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mWindowAttributes.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mWindowAttributes.format = PixelFormat.RGBA_8888;
        // not disturbing normal usage scenario
        mWindowAttributes.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // set translucent navigation bar to avoid UI flicker
            //mWindowAttributes.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            mWindowAttributes.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        }
        // relative to left-top
        mWindowAttributes.gravity = Gravity.CENTER;
        // not support rotation
        try {
            mWindowAttributes.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } catch (NoSuchFieldError e) {
            mWindowAttributes.screenOrientation = 1;
        }
        // initialize docking point
        mWindowAttributes.x = 0;
        mWindowAttributes.y = 0;
        // initialize width and height
        mWindowAttributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowAttributes.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    public void setContentView(View layout) {
        this.setContentView(layout, false);
    }

    /**
     * @param layout target layout to set as content view
     * @param isFloating true means floating, false means fixed
     * Note: If isFloating is set as true, it means view root's touch events are intercepted inside
     *       the smart window, then setOnTouchListener() request will be ignored so you could not set
     *       external touch listener any more.
     */
    public void setContentView(View layout, boolean isFloating) {
        mViewRoot = layout;
        mIsFloating = isFloating;
        initViewRoot();

        // handle key events
        mViewRoot.setFocusableInTouchMode(true);
        mViewRoot.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        if (mOnBackPressedListener != null) {
                            mOnBackPressedListener.onBackPressed();
                        }
                        break;
                }

                return false;
            }
        });
    }

    public void setContentView(int layoutResID) {
        setContentView(layoutResID, false);
    }


    /**
     * @param layoutResID target layout resource ID to set as content view
     * @param isFloating true means floating, false means fixed
     * Note: If isFloating is set as true, it means view root's touch events are intercepted inside
     *       the smart window, then setOnTouchListener() request will be ignored so you could not set
     *       external touch listener any more.
     */
    public void setContentView(int layoutResID, boolean isFloating) {
        mViewRoot = LayoutInflater.from(mContext).inflate(layoutResID, null);
        mIsFloating = isFloating;
        initViewRoot();
    }

    private void initViewRoot() {
        // do manual measure to get view width and height
        if (mWindowAttributes.width == 0 || mWindowAttributes.height == 0) {
            mViewRoot.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            mWindowAttributes.width = mViewRoot.getMeasuredWidth();
            mWindowAttributes.height = mViewRoot.getMeasuredHeight();
        }

        if (mIsFloating) {
            mViewRoot.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // get status bar height
                    int statusBarHeight = getStatusBarHeight();

                    // get coordinate relative to screen
                    mXInScreen = event.getRawX();
                    mYInScreen = event.getRawY() - statusBarHeight;

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // get coordinate relative to view
                            mXInView = event.getX();
                            mYInView = event.getY();
                            mXDownInScreen = event.getRawX();
                            mYDownInScreen = event.getRawY() - statusBarHeight;
                            // for long click event
                            mIsMoved = false;
                            mIsLongClicked = false;
                            mLongClickHandler.postDelayed(mLongClickRunnable, LONG_CLICK_INTERVAL);
                            break;
                        case MotionEvent.ACTION_UP:
                            if (mIsMoved || mIsLongClicked) {
                                break;
                            }

                            // cancel long click event processing
                            mLongClickHandler.removeCallbacks(mLongClickRunnable);

                            if (Math.abs(mXDownInScreen - mXInScreen) < TOUCH_OFFSET_THRESHOLD
                                    && Math.abs(mYDownInScreen - mYInScreen) < TOUCH_OFFSET_THRESHOLD) {
                                // handle click event
                                if (mOnClickListener != null) {
                                    mOnClickListener.onClick(mContext,
                                            mWindowAttributes.x, mWindowAttributes.y,
                                            mWindowAttributes.width, mWindowAttributes.height);
                                }
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            updateViewPosition();
                            if (!mIsMoved) {
                                if (Math.abs(mXDownInScreen - mXInScreen) > TOUCH_OFFSET_THRESHOLD
                                        && Math.abs(mYDownInScreen - mYInScreen) > TOUCH_OFFSET_THRESHOLD) {
                                    mIsMoved = true;
                                    mLongClickHandler.removeCallbacks(mLongClickRunnable);
                                }
                            }
                            break;
                    }

                    // return false to transfer event to other handlers
                    return false;
                }

                private void updateViewPosition() {
                    mWindowAttributes.x = (int) (mXInScreen - mXInView);
                    mWindowAttributes.y = (int) (mYInScreen - mYInView);
                    mWindowManager.updateViewLayout(mViewRoot, mWindowAttributes);
                }

                private int getStatusBarHeight() {
                    Rect frame = new Rect();
                    mViewRoot.getWindowVisibleDisplayFrame(frame);
                    return frame.top;
                }
            });
        }
    }


    public void show() {
        mWindowManager.addView(mViewRoot, mWindowAttributes);
        mIsVisible = true;
    }

    public void hide() {
        mWindowManager.removeView(mViewRoot);
        mIsVisible = false;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        if (!mIsFloating) {
            mViewRoot.setOnTouchListener(listener);
        }
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.mOnLongClickListener = listener;
    }

    public void setOnBackPressedListener(OnBackPressedListener listener) {
        this.mOnBackPressedListener = listener;
    }

    public int getWidth() {
        return mWindowAttributes.width;
    }

    public void setWidth(int width) {
        mWindowAttributes.width = width;
    }

    public int getHeight() {
        return mWindowAttributes.height;
    }

    public void setHeight(int height) {
        mWindowAttributes.height = height;
    }

    public View getViewRoot() {
        return mViewRoot;
    }

    public void setFlags(int flags) {
        mWindowAttributes.flags = flags;
    }

    public int getFlags() {
        return mWindowAttributes.flags;
    }

    public void setGravity(int gravity) {
        mWindowAttributes.gravity = gravity;
    }

    public int getGravity() {
        return mWindowAttributes.gravity;
    }

    public void setPosition(int x, int y) {
        mWindowAttributes.x = x;
        mWindowAttributes.y = y;
    }

    public Point getPosition() {
        return new Point(mWindowAttributes.x, mWindowAttributes.y);
    }

    public boolean isVisible() {
        return mIsVisible;
    }
}