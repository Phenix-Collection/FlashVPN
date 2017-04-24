package com.polestar.multiaccount.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;

/**
 * Created by guojia on 2016/12/16.
 */

public class TutorialGuides implements PopupWindow.OnDismissListener {

    private static final int mDefaultPopupWindowStyleRes = android.R.attr.popupWindowStyle;
    private static final int mDefaultBackgroundColorRes = R.color.guidetooltip_background;
    private static final int mDefaultTextColorRes = R.color.white;
    private static final int mDefaultTextSizeRes = R.dimen.guidetooltip_text_size;
    private static final int mDefaultPaddingRes = R.dimen.guidetooltip_padding;
    private static final int mDefaultArrowWidthRes = R.dimen.guidetooltip_arrow_width;
    private static final int mDefaultArrowHeightRes = R.dimen.guidetooltip_arrow_height;
    private static final int mDefaultArrowColorRes = R.color.guidetooltip_arrow;
    private static final int mDefaultAnimationPaddingRes = R.dimen.guidetooltip_animation_padding;
    private static final int mDefaultAnimationDurationRes = 3000;
    private static final int mDefaultMarginRes = R.dimen.guidetooltip_margin;
    private static final int mDefaultMaxWidthRes = R.dimen.guidetooltip_maxwidth;

    private PopupWindow mPopupWindow;

    private final Context mContext;
    private final View mAnchorView;
    private final View mContentView;
    private final String mText;
    private final int mTextViewId;
    private final boolean mDismissOnInsideTouch;
    private final boolean mDismissOnOutsideTouch;
    private final  boolean mModal;
    private final float mPadding;
    private final float mMargin;
    private final boolean mShowArrow;
    private View mContentLayout;
    private ImageView mArrowView;
    private final Drawable mArrowDrawable;
    private final int mGravity;
    private final float mArrowWidth;
    private final float mArrowHeight;
    private final boolean mAnimated;
    private final int mAnimationPadding;
    private final long mAnimationDuration;
    private View mOverlay;
    private final ViewGroup mRootView;
    private final float mMaxWidth;
    private RectF mRectF = null;

    private final OnShowListener mOnShowListener;
    private final OnDismissListener mOnDismissListener;

    private AnimatorSet mAnimator;

    private boolean dismissed = false;
    private boolean mDefaultBackground;

    public TutorialGuides(Builder builder){
        mContext = builder.context;
        mAnchorView = builder.anchorView;
        mDismissOnInsideTouch = builder.dismissOnInsideTouch;
        mDismissOnOutsideTouch = builder.dismissOnOutsideTouch;
        mModal = builder.modal;
        mText = builder.text;
        mTextViewId = builder.textViewId;
        mContentView = builder.contentView;
        mPadding = builder.padding;
        mMargin = builder.margin;
        mShowArrow =  builder.showArrow;
        mArrowDrawable = builder.arrowDrawable;
        mGravity = builder.gravity;
        mArrowWidth = builder.arrowWidth;
        mArrowHeight = builder.arrowHeight;
        mAnimated = builder.animated;
        mAnimationPadding = builder.animationPadding;
        mAnimationDuration = builder.animationDuration;
        mRootView = (ViewGroup) mAnchorView.getRootView();
        mMaxWidth = builder.maxWidth;
        mOnShowListener = builder.onShowListener;
        mOnDismissListener = builder.onDismissListener;
        mRectF = builder.rectF;
        mDefaultBackground = builder.defaultBackground;

        init();
    }

    private void init() {
        configPopupWindow();
        configContentView();
    }

    private void configPopupWindow() {
        mPopupWindow = new PopupWindow(mContext, null, mDefaultPopupWindowStyleRes);
        mPopupWindow.setOnDismissListener(this);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setClippingEnabled(false);
        if (mDismissOnInsideTouch || mDismissOnOutsideTouch)
            mPopupWindow.setTouchInterceptor(mPopupWindowsTouchListener);

    }

    private void configContentView() {
        if (mContentView instanceof TextView) {
            TextView tv = (TextView) mContentView;
            tv.setText(mText);
            if (mDefaultBackground){
                mContentView.setBackgroundResource(R.drawable.guidetooltip_background_default);
            }else {
                mContentView.setBackgroundResource(R.drawable.guidetooltip_background);
            }
            mContentView.setPadding((int) mPadding, (int) mPadding, (int) mPadding, (int) mPadding);
        } else {
            if (mTextViewId!=-1) {
                TextView tv = (TextView) mContentView.findViewById(mTextViewId);
                if (tv != null)
                    tv.setText(mText);
            }
        }

        if (mShowArrow){
            mArrowView = new ImageView(mContext);
            mArrowView.setImageDrawable(mArrowDrawable);
            LinearLayout.LayoutParams arrowLayoutParams;
            if (mGravity == Gravity.TOP || mGravity == Gravity.BOTTOM) {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) mArrowWidth, (int) mArrowHeight, 0);
            } else {
                arrowLayoutParams = new LinearLayout.LayoutParams((int) mArrowHeight, (int) mArrowWidth, 0);
            }
            mArrowView.setLayoutParams(arrowLayoutParams);

            LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(mGravity == Gravity.START || mGravity == Gravity.END ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            int padding = mAnimated ? mAnimationPadding : (int) DisplayUtils.dip2px(mContext, 4);
            linearLayout.setPadding(padding, padding, padding, (mGravity == Gravity.TOP && !mAnimated) ? 0 : padding);

            if (mGravity == Gravity.TOP || mGravity == Gravity.START) {
                linearLayout.addView(mContentView);
                linearLayout.addView(mArrowView);
            } else {
                linearLayout.addView(mArrowView);
                linearLayout.addView(mContentView);
            }

            LinearLayout.LayoutParams contentViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            contentViewParams.gravity = Gravity.CENTER;
            mContentView.setLayoutParams(contentViewParams);

            mContentLayout = linearLayout;
        }else{
            mContentLayout = mContentView;
            mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        mContentLayout.setVisibility(View.INVISIBLE);
        mPopupWindow.setContentView(mContentLayout);
    }

    private final View.OnTouchListener mPopupWindowsTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getX() > 0 && event.getX() < v.getWidth() &&
                    event.getY() > 0 && event.getY() < v.getHeight()) {
                if (mDismissOnInsideTouch) {
                    dismiss();
                    return mModal;
                }
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return mModal;
        }
    };

    public void dismiss(){
        if (dismissed) return;

        dismissed = true;
        if (mPopupWindow != null ){
            try {
                mPopupWindow.dismiss();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void show() {
        MLogs.e("show");
        verifyDismissed();

        if (!isActivityValid()){
            MLogs.e("activity not valid");
            return;
        }

        mContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(mLocationLayoutListener);
        mContentLayout.getViewTreeObserver().addOnGlobalLayoutListener(mAutoDismissLayoutListener);

        try {
            mRootView.post(new Runnable() {
                @Override
                public void run() {
                    dismissed = false;
                    try {
                        mPopupWindow.showAtLocation(mRootView, Gravity.NO_GRAVITY, mRootView.getWidth(), mRootView.getHeight());
                        MLogs.d("show at location");
                    }catch (Exception e) {
                        MLogs.logBug(MLogs.getStackTraceString(e));
                    }
                }
            });
        }catch (Exception ex){
        }
    }

    private boolean isActivityValid() {
        if (null != mContext && mContext instanceof Activity) {
            Activity at = (Activity) mContext;
            if (at.isFinishing()) {
                // /< 是 activity，但已finish
                return false;
            } else {
                // /< 是activity，还在运行中...
                return true;
            }
        } else {
            // /< context无效 或 context不是有效的activity
            return false;
        }
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mLocationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (dismissed)
                return;

            if (mMaxWidth > 0 && (mContentView.getWidth()-2*mPadding) > mMaxWidth) {
                TutorialGuidesUtils.setWidth(mContentView, mMaxWidth+2*mPadding);
                mPopupWindow.update(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return;
            }

            TutorialGuidesUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), this);
            mPopupWindow.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mArrowLayoutListener);
            PointF location = calculePopupLocation();
            mPopupWindow.setClippingEnabled(true);
            mPopupWindow.update((int) location.x, (int) location.y, mPopupWindow.getWidth(), mPopupWindow.getHeight());
            mPopupWindow.getContentView().requestLayout();
            createOverlay();
        }
    };

    private void createOverlay() {
        mOverlay = new View(mContext);
        mOverlay.setBackgroundColor(Color.TRANSPARENT);
        mOverlay.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mOverlay.setOnTouchListener(mOverlayTouchListener);
        mRootView.addView(mOverlay);
    }

    private final View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mDismissOnOutsideTouch) {
                dismiss();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return mModal;
        }
    };

    private PointF calculePopupLocation() {
        PointF location = new PointF();
        final PointF anchorCenter;
        final RectF anchorRect;
        if (mRectF == null){
            anchorRect = TutorialGuidesUtils.getRectFOnScreen(mAnchorView);
            anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());
        }else{
            anchorRect = mRectF;
            anchorCenter = new PointF(anchorRect.centerX(), anchorRect.centerY());
        }

        switch (mGravity) {
            case Gravity.START:
                location.x = anchorRect.left - mPopupWindow.getContentView().getWidth() - mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.END:
                location.x = anchorRect.right + mMargin;
                location.y = anchorCenter.y - mPopupWindow.getContentView().getHeight() / 2f;
                break;
            case Gravity.TOP:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorRect.top - mPopupWindow.getContentView().getHeight() - mMargin;
                break;
            case Gravity.BOTTOM:
                location.x = anchorCenter.x - mPopupWindow.getContentView().getWidth() / 2f;
                location.y = anchorRect.bottom + mMargin;
                break;
            default:
                throw new IllegalArgumentException("Gravity must have be START, END, TOP or BOTTOM.");
        }

        return location;
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mShowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (dismissed)
                return;

            TutorialGuidesUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), this);
            if (mOnShowListener != null) {
                mOnShowListener.onShow(TutorialGuides.this);
            }
            mContentLayout.setVisibility(View.VISIBLE);
        }
    };


    private final ViewTreeObserver.OnGlobalLayoutListener mArrowLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (dismissed)
                return;

            TutorialGuidesUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), this);
            mPopupWindow.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mAnimationLayoutListener);
            mPopupWindow.getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mShowLayoutListener);
            if (mShowArrow) {
                RectF achorRect;
                if (mRectF == null){
                    achorRect = TutorialGuidesUtils.getRectFOnScreen(mAnchorView);
                }else{
                    achorRect = mRectF;
                }
                RectF contentViewRect = TutorialGuidesUtils.getRectFOnScreen(mContentLayout);
                float x, y;
                if (mGravity == Gravity.BOTTOM || mGravity == Gravity.TOP) {
                    x = mContentLayout.getPaddingLeft() + TutorialGuidesUtils.pxFromDp(2);
                    float centerX = (contentViewRect.width() / 2f) - (mArrowView.getWidth() / 2f);
                    float newX = centerX - (contentViewRect.centerX() - achorRect.centerX());
                    if (newX > x) {
                        if (newX + mArrowView.getWidth() + x > contentViewRect.width()) {
                            x = contentViewRect.width() - mArrowView.getWidth() - x;
                        } else {
                            x = newX;
                        }
                    }
                    y = mArrowView.getTop();
                    y = y + (mGravity == Gravity.TOP ? -1 : +1);
                } else {
                    y = mContentLayout.getPaddingTop() + TutorialGuidesUtils.pxFromDp(2);
                    float centerY = (contentViewRect.height() / 2f) - (mArrowView.getHeight() / 2f);
                    float newY = centerY - (contentViewRect.centerY() - achorRect.centerY());
                    if (newY > y) {
                        if (newY + mArrowView.getHeight() + y > contentViewRect.height()) {
                            y = contentViewRect.height() - mArrowView.getHeight() - y;
                        } else {
                            y = newY;
                        }
                    }
                    x = mArrowView.getLeft();
                    x = x + (mGravity == Gravity.START ? -1 : +1);
                }
                TutorialGuidesUtils.setX(mArrowView, (int) x);
                TutorialGuidesUtils.setY(mArrowView, (int) y);
            }
            mPopupWindow.getContentView().requestLayout();
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener mAnimationLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mPopupWindow != null) {
                TutorialGuidesUtils.removeOnGlobalLayoutListener(mPopupWindow.getContentView(), this);
            }
            if (dismissed)
                return;

            if (mAnimated) {
                startAnimation();
            }
            if (mPopupWindow != null) {
                mPopupWindow.getContentView().requestLayout();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startAnimation() {
        final String property = mGravity == Gravity.TOP || mGravity == Gravity.BOTTOM ? "translationY" : "translationX";

        final ObjectAnimator anim1 = ObjectAnimator.ofFloat(mContentLayout, property, -mAnimationPadding, mAnimationPadding);
        anim1.setDuration(mAnimationDuration);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());

        final ObjectAnimator anim2 = ObjectAnimator.ofFloat(mContentLayout, property, mAnimationPadding, -mAnimationPadding);
        anim2.setDuration(mAnimationDuration);
        anim2.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimator = new AnimatorSet();
        mAnimator.playSequentially(anim1, anim2);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!dismissed && isShowing()) {
                    animation.start();
                }
            }
        });
        mAnimator.start();
    }

    private final ViewTreeObserver.OnGlobalLayoutListener mAutoDismissLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (dismissed)
                return;

            if (!mRootView.isShown())
                dismiss();
        }
    };


    private void verifyDismissed() {
        if (dismissed) {
            throw new IllegalArgumentException("Tooltip has ben dismissed.");
        }
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    @Override
    public void onDismiss() {
        dismissed = true;

        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.end();
            mAnimator.cancel();
        }

        if (mRootView != null && mOverlay != null) {
            mRootView.removeView(mOverlay);
        }

        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this);
        }

        mPopupWindow = null;
    }


    public interface OnShowListener {
        void onShow(TutorialGuides tooltip);
    }

    public interface OnDismissListener {
        void onDismiss(TutorialGuides tooltip);
    }

    /**********************************************************************************************/
    public static class Builder {
        private final Context context;
        private View anchorView;
        private RectF rectF = null;
        private boolean dismissOnInsideTouch = true;
        private boolean dismissOnOutsideTouch = true;
        private boolean modal = false;
        @IdRes
        private int textViewId = android.R.id.text1;
        private String text = "";

        private View contentView = null;
        private int backgroundColor = 0;
        private int textColor = 0;
        private int textSize = 0;
        private float padding = -1;
        private float margin = -1;
        private boolean showArrow = true;
        private float arrowHeight;
        private float arrowWidth;
        private Drawable arrowDrawable;
        private int arrowColor = 0;
        private int gravity = Gravity.BOTTOM;
        private boolean animated = false;
        private int animationPadding;
        private long animationDuration;
        private float maxWidth = 0;
        private boolean defaultMaxWidth = false;
        private boolean defaultBackground = false;

        private OnShowListener onShowListener;
        private OnDismissListener onDismissListener;
        private Resources resources;
        private TutorialGuides tutorialGuides;

        public Builder(Context context) {
            this.context = context;
            this.resources = context.getResources();
        }

        private Resources getResources(){
            return resources;
        }

        public TutorialGuides build(){
            validateArguments();

            if (backgroundColor == 0) {
                backgroundColor = getResources().getColor(mDefaultBackgroundColorRes);
            }
            if (textColor == 0) {
                textColor = getResources().getColor(mDefaultTextColorRes);
            }
            if (textSize == 0){
                textSize = getResources().getDimensionPixelSize(mDefaultTextSizeRes);
            }
            if (defaultMaxWidth){
                maxWidth = getResources().getDimension(mDefaultMaxWidthRes);
            }
            if (contentView == null){
                TextView tv = new TextView(context);
//                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(textColor);
                tv.setTextSize(TutorialGuidesUtils.dpFromPx(textSize));
                tv.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                contentView = tv;
            }

            if (arrowColor == 0) {
                arrowColor = getResources().getColor(mDefaultArrowColorRes);
            }

            if (arrowDrawable == null) {
                int arrowDirection = TutorialGuidesUtils.tooltipGravityToArrowDirection(gravity);
                arrowDrawable = new ArrowDrawable(arrowColor, arrowDirection);
            }

            if (margin == -1) {
                margin = getResources().getDimension(mDefaultMarginRes);
            }

            if (padding < 0) {
                padding = getResources().getDimensionPixelSize(mDefaultPaddingRes);
            }

            if (animationPadding == 0) {
                animationPadding = getResources().getDimensionPixelSize(mDefaultAnimationPaddingRes);
            }
            if (animationDuration == 0) {
                animationDuration = mDefaultAnimationDurationRes;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                animated = false;
            }
            if (showArrow) {
                if (arrowWidth == 0)
                    arrowWidth = getResources().getDimension(mDefaultArrowWidthRes);
                if (arrowHeight == 0)
                    arrowHeight = getResources().getDimension(mDefaultArrowHeightRes);
            }

            tutorialGuides = new TutorialGuides(this);
            return tutorialGuides;
        }

        public void dismiss(){
            if (tutorialGuides != null){
                tutorialGuides.dismiss();
            }
        }

        public boolean isShowing(){
            if (tutorialGuides != null){
                return tutorialGuides.isShowing();
            }
            return false;
        }

        private void validateArguments() throws IllegalArgumentException {
            if (context == null) {
                throw new IllegalArgumentException("Context not specified.");
            }
            if (anchorView == null) {
                MLogs.e("Anchor view not specified.");
                throw new IllegalArgumentException("Anchor view not specified.");
            }
        }

        public Builder anchorView(View anchorView) {
            this.anchorView = anchorView;
            return this;
        }

        public Builder anchorView(View anchorView, RectF rectF) {
            this.anchorView = anchorView;
            this.rectF = rectF;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder gravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder maxWidth(@DimenRes int maxWidthRes) {
            this.maxWidth = getResources().getDimension(maxWidthRes);
            return this;
        }

        public Builder defaultMaxWidth(boolean defaultMaxWidth){
            this.defaultMaxWidth = defaultMaxWidth;
            return this;
        }

        public Builder maxWidth(float maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder arrowColor(@ColorInt int arrowColor) {
            this.arrowColor = arrowColor;
            return this;
        }

        public Builder arrowColor2(@ColorRes int arrowColor) {
            this.arrowColor = getResources().getColor(arrowColor);
            return this;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animated(boolean animated) {
            this.animated = animated;
            return this;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public Builder animationDuration(long animationDuration) {
            this.animationDuration = animationDuration;
            return this;
        }

        public Builder animationPadding(int animationPadding) {
            this.animationPadding = animationPadding;
            return this;
        }

        public Builder arrowDrawable(Drawable arrowDrawable) {
            this.arrowDrawable = arrowDrawable;
            return this;
        }

        public Builder arrowDrawable(@DrawableRes int drawableRes) {
            this.arrowDrawable = getResources().getDrawable(drawableRes);
            return this;
        }

        public Builder arrowHeight(float arrowHeight) {
            this.arrowHeight = arrowHeight;
            return this;
        }

        public Builder arrowWidth(float arrowWidth) {
            this.arrowWidth = arrowWidth;
            return this;
        }

        public Builder backgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder textSize(int textSize){
            this.textSize = textSize;
            return this;
        }

        public Builder showArrow(boolean showArrow) {
            this.showArrow = showArrow;
            return this;
        }

        public Builder margin(float margin) {
            this.margin = margin;
            return this;
        }

        public Builder defaultBackground(boolean defaultBackground){
            this.defaultBackground = defaultBackground;
            return this;
        }

        public Builder margin(@DimenRes int marginRes) {
            this.margin = getResources().getDimension(marginRes);
            return this;
        }

        public Builder padding(float padding) {
            this.padding = padding;
            return this;
        }

        public Builder padding(@DimenRes int paddingRes) {
            this.padding = getResources().getDimension(paddingRes);
            return this;
        }

        public Builder contentView(TextView textView) {
            this.contentView = textView;
            this.textViewId = 0;
            return this;
        }

        public Builder contentView(View contentView, @IdRes int textViewId) {
            this.contentView = contentView;
            this.textViewId = textViewId;
            return this;
        }

        public Builder contentView(@LayoutRes int contentViewId, @IdRes int textViewId) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.contentView = inflater.inflate(contentViewId, null, false);
            this.textViewId = textViewId;
            return this;
        }

        public Builder contentView(@LayoutRes int contentViewId) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.contentView = inflater.inflate(contentViewId, null, false);
            this.textViewId = 0;
            return this;
        }

        public Builder dismissOnInsideTouch(boolean dismissOnInsideTouch) {
            this.dismissOnInsideTouch = dismissOnInsideTouch;
            return this;
        }

        public Builder dismissOnOutsideTouch(boolean dismissOnOutsideTouch) {
            this.dismissOnOutsideTouch = dismissOnOutsideTouch;
            return this;
        }

        public Builder modal(boolean modal) {
            this.modal = modal;
            return this;
        }

        public Builder onShowListener(OnShowListener onShowListener) {
            this.onShowListener = onShowListener;
            return this;
        }

        public Builder onDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }
    }

}