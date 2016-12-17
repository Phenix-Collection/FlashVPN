package com.polestar.multiaccount.widgets;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.widgets.dragdrop.DragSource;
import com.polestar.multiaccount.widgets.dragdrop.DragView;
import com.polestar.multiaccount.widgets.dragdrop.DropTarget;

/**
 * Created by yxx on 2016/8/11.
 */
public class CustomFloatView extends View implements DropTarget{

    public static final int ANIM_DURATION = 500;
    private static final int STATE_IDLE = 1;
    private static final int STATE_ANIMATION_TO_EXTAND = 2;
    private static final int STATE_ANIMATION_TO_IDLE = 3;
    private static final int STATE_EXTAND = 4;
    private static final int STATE_BTN_LEFT_TO_RIGHT = 5;
    private static final int STATE_BTN_RIGHT_TO_LEFT = 6;

    public static final int SELECT_BTN_NONE = 0;
    public static final int SELECT_BTN_LEFT = 1;
    public static final int SELECT_BTN_RIGHT = 2;

    private int currentState = STATE_IDLE;
    private int nextState = STATE_IDLE;
    private Paint mPaint;
    private int outerColor;
    private float outerStrokeWidth;
    private float defaultOuterStrokeWidth;
    private int innerColor;
    private int innerPaddingWidth;
    private int defaultInnerPaddingWidth;
    private int contentPadding;
    private int defaultContentPadding;
    //    private int diameter;
    private int defaultPadding;
    private int leftAndRightSidePadding;
    private int centerRectWidth;

    private RectF mRectF;
    private RectF leftBtnRect;
    private RectF rightBtnRect;
    private RectF btnBgRect;
    private int btnWidth;
    private int lineWidth;
    private int centerDotColor;
    private int centerDotWidth;
    private float btnExtraWidth;
    private RectF centerDotRect;
    private Bitmap leftBtnBitmap, rightBtnBitmap;
    private int selectedBtn = SELECT_BTN_NONE;
    private int roteDegree;
    private boolean cancleAnim;

    public CustomFloatView(Context context) {
        super(context);
        init();
    }

    public int getSelectedState(){
        return selectedBtn;
    }

    public CustomFloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public boolean isIdle(){
        return currentState == STATE_IDLE;
    }

    public void restore(){
        if(currentState != STATE_IDLE){
            cancleAnim = true;
            nextState = STATE_IDLE;
        }
    }

    private void cancleAnim(){
        selectedBtn = SELECT_BTN_NONE;
        resetState();
        cancleAnim = false;
        invalidate();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(DisplayUtils.dip2px(getContext(), 1));

        mRectF = new RectF();
        leftBtnRect = new RectF();
        rightBtnRect = new RectF();
        centerDotRect = new RectF();
        btnBgRect = new RectF();

        lineWidth = DisplayUtils.dip2px(getContext(), 2);
        outerColor = getResources().getColor(R.color.add_app_color);
        defaultOuterStrokeWidth = DisplayUtils.dip2px(getContext(), 5);
        outerStrokeWidth = defaultOuterStrokeWidth;

        innerColor = getResources().getColor(R.color.left_right_anim_color);
        defaultInnerPaddingWidth = DisplayUtils.dip2px(getContext(), 4);
        innerPaddingWidth = defaultInnerPaddingWidth;

        defaultContentPadding = DisplayUtils.dip2px(getContext(), 13);
        contentPadding = defaultContentPadding;

        defaultPadding = DisplayUtils.dip2px(getContext(), 5);
        leftAndRightSidePadding = DisplayUtils.dip2px(getContext(),16);
        centerDotColor = getResources().getColor(R.color.white);
        centerDotWidth = DisplayUtils.dip2px(getContext(), 2);
    }

    public void startBreath() {
//        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "outerStrokeWidth", 0.5f, defaultOuterStrokeWidth);
//        animator.setRepeatMode(ObjectAnimator.REVERSE);
//        animator.setRepeatCount(Integer.MAX_VALUE);
//        animator.setDuration(1200);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//            }
//        });
//        animator.start();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.5f,1.3f);
        valueAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        valueAnimator.setRepeatCount(Integer.MAX_VALUE);
        valueAnimator.setDuration(1200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(currentState == STATE_IDLE){
                    float currentValue = (float) valueAnimator.getAnimatedValue();
                    outerStrokeWidth = defaultOuterStrokeWidth * currentValue;
                    invalidate();
                }else{
                    float lastWidth = outerStrokeWidth;
                    outerStrokeWidth = defaultOuterStrokeWidth;
                    if(lastWidth != defaultOuterStrokeWidth){
                        invalidate();
                    }
                }
            }
        });
//        valueAnimator.start();
    }

    public void animToIdel() {
        if(!isIdle(STATE_ANIMATION_TO_IDLE)){
            return;
        }
        currentState = STATE_ANIMATION_TO_IDLE;
        ValueAnimator extandsAnimator = ValueAnimator.ofFloat(1f, 0f);
        extandsAnimator.setInterpolator(new AnticipateOvershootInterpolator(2f));
        extandsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(cancleAnim){
                    extandsAnimator.cancel();
                    cancleAnim();
                    return;
                }
                float currentValue = (float) valueAnimator.getAnimatedValue();
                centerRectWidth = (int) ((getContentWidth() - getHeight() + 2 * outerStrokeWidth) * currentValue);
                innerPaddingWidth = (int) (defaultInnerPaddingWidth + (getHeight() / 2 - defaultInnerPaddingWidth - defaultPadding) * currentValue);
                if (currentValue == 0f) {
                    resetState();
                    handleNextState(STATE_IDLE);
                }
                invalidate();
            }
        });
        extandsAnimator.setDuration(ANIM_DURATION);
        extandsAnimator.start();
    }

    public void animToExtands() {
        if(!isIdle(STATE_ANIMATION_TO_EXTAND)){
            return;
        }
        currentState = STATE_ANIMATION_TO_EXTAND;

        ValueAnimator extandsAnimator = ValueAnimator.ofFloat(0f, 1f);
        extandsAnimator.setInterpolator(new AnticipateOvershootInterpolator(2f));
        extandsAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(cancleAnim){
                    extandsAnimator.cancel();
                    cancleAnim();
                    return;
                }
                float currentValue = (float) valueAnimator.getAnimatedValue();
                innerPaddingWidth = (int) (defaultInnerPaddingWidth + (getHeight() / 2 - defaultInnerPaddingWidth - defaultPadding) * currentValue);
                if(innerPaddingWidth > defaultInnerPaddingWidth){
                    centerRectWidth = (int) ((getContentWidth() - getHeight() + 2 * outerStrokeWidth) * currentValue);
                }
                if (currentValue == 1f) {
                   handleNextState(STATE_EXTAND);
                }
                invalidate();
            }
        });
        extandsAnimator.setDuration(ANIM_DURATION);
        extandsAnimator.start();
    }

    private void animChangeBtn(int btn) {
        if(btn == selectedBtn)
            return;
        if(selectedBtn == SELECT_BTN_NONE){
            selectedBtn = btn;
            invalidate();
            return;
        }
        if(btn == SELECT_BTN_NONE){
            selectedBtn = btn;
            invalidate();
        }else if(btn == SELECT_BTN_LEFT){
            animRightToleft();
        }else{
            animLeftToRight();
        }
    }

    private void animLeftToRight(){
        if(!isIdle(STATE_BTN_LEFT_TO_RIGHT)){
            return;
        }
        selectedBtn = SELECT_BTN_RIGHT;
        currentState = STATE_BTN_LEFT_TO_RIGHT;
        btnBgRect.set(leftBtnRect);
        btnExtraWidth = 0;
        final float offset = rightBtnRect.left - leftBtnRect.left;
        final float width = leftBtnRect.right - leftBtnRect.left;
        ValueAnimator leftToRightAnim = ValueAnimator.ofFloat(0f, 1f);
        leftToRightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        leftToRightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(cancleAnim){
                    leftToRightAnim.cancel();
                    cancleAnim();
                    return;
                }
                float currentValue = (float) valueAnimator.getAnimatedValue();
                btnBgRect.right = btnBgRect.right - btnExtraWidth;
                btnBgRect.offsetTo(offset * currentValue +leftBtnRect.left,leftBtnRect.top);
                if(currentValue < 0.5f){
                    btnExtraWidth = width * currentValue * 0.5f;
                }else{
                    btnExtraWidth = width * (1f - currentValue) * 0.5f;
                }
                btnBgRect.right = btnBgRect.right + btnExtraWidth;
                if(currentValue == 1f){
                   handleNextState(STATE_EXTAND);
                }
                invalidate();
            }
        });
        leftToRightAnim.setDuration(ANIM_DURATION);
        leftToRightAnim.start();
    }

    private void animRightToleft(){
        if(!isIdle(STATE_BTN_RIGHT_TO_LEFT)){
            return;
        }
        selectedBtn = SELECT_BTN_LEFT;
        currentState = STATE_BTN_RIGHT_TO_LEFT;
        btnBgRect.set(rightBtnRect);
        final float offset = rightBtnRect.left - leftBtnRect.left;
        final float width = rightBtnRect.right - rightBtnRect.left;
        ValueAnimator rightToLeftAnim = ValueAnimator.ofFloat(0f, 1f);
        rightToLeftAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        rightToLeftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if(cancleAnim){
                    rightToLeftAnim.cancel();
                    cancleAnim();
                    return;
                }
                float currentValue = (float) valueAnimator.getAnimatedValue();
                btnBgRect.left = btnBgRect.left + btnExtraWidth;
                btnBgRect.offsetTo(- offset * currentValue + rightBtnRect.left,rightBtnRect.top);
                if(currentValue < 0.5f){
                    btnExtraWidth = width * currentValue * 0.5f;
                }else{
                    btnExtraWidth = width * (1f - currentValue) * 0.5f;
                }
                btnBgRect.left = btnBgRect.left - btnExtraWidth;
                if(currentValue == 1f){
                  handleNextState(STATE_EXTAND);
                }
                invalidate();
            }
        });
        rightToLeftAnim.setDuration(ANIM_DURATION);
        rightToLeftAnim.start();
    }

    public void startRote(){
        ValueAnimator roterAnimator = ValueAnimator.ofInt(0,360 * 2);
        roterAnimator.setInterpolator(new OvershootInterpolator(1f));
        roterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int curValue = (int) valueAnimator.getAnimatedValue();
                roteDegree = curValue % 360;
                if(curValue == 360 * 2){
                    roteDegree = 0;
                }
                invalidate();
            }
        });
        roterAnimator.setDuration(800);
        roterAnimator.start();
    }


    private boolean isIdle(int state){
        if(currentState == STATE_IDLE || currentState == STATE_EXTAND){
            return true;
        }
        if(state == currentState){
            nextState = STATE_IDLE;
            return false;
        }else{
            nextState = state;
            return false;
        }
    }

    private void handleNextState(int state){
        currentState = state;
        if (nextState == STATE_ANIMATION_TO_EXTAND) {
            animToExtands();
        }else if (nextState == STATE_ANIMATION_TO_IDLE) {
            animToIdel();
        }else if(nextState == STATE_BTN_LEFT_TO_RIGHT){
            animChangeBtn(SELECT_BTN_LEFT);
        }else if(nextState == STATE_BTN_RIGHT_TO_LEFT){
            animChangeBtn(SELECT_BTN_RIGHT);
        }
        nextState = STATE_IDLE;
    }

    private void resetState() {
        innerPaddingWidth = defaultInnerPaddingWidth;
        contentPadding = defaultContentPadding;
        centerRectWidth = 0;
        currentState = STATE_IDLE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(!isPointInInnerCircle(event.getX(),event.getY())){
                    MLogs.e("onclick -- ignore");
                    return false;
                }
                break;
        }
        MLogs.e("onclick -- sure");
        return super.onTouchEvent(event);
    }

    private boolean isPointInInnerCircle(float x, float y){
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = getHeight() / 2 - defaultPadding - innerPaddingWidth + 10;
        if(radius > 0 && (centerX - x) * (centerX - x) + (centerY - y) * (centerY - y) <= radius * radius){
            return true;
        }
        return false;
    }

    private int getContentWidth(){
        return getWidth() - leftAndRightSidePadding * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        mPaint.setColor(outerColor);
        mPaint.setStyle(Paint.Style.FILL);

        float height = getHeight() - defaultPadding * 2;
        mRectF.left = centerX - height / 2 + outerStrokeWidth - centerRectWidth / 2;
        mRectF.right = centerX + height / 2 - outerStrokeWidth + centerRectWidth / 2;
        mRectF.top = defaultPadding + outerStrokeWidth;
        mRectF.bottom = getHeight() - defaultPadding - outerStrokeWidth;

        drawHalfCircleRect(canvas, mRectF, centerRectWidth);

        if (currentState == STATE_EXTAND || currentState == STATE_BTN_LEFT_TO_RIGHT || currentState == STATE_BTN_RIGHT_TO_LEFT) {
            btnWidth = getContentWidth() * 3 / 10;
            leftBtnRect.left = mRectF.left + outerStrokeWidth;
            leftBtnRect.right = leftBtnRect.left + btnWidth;
            leftBtnRect.top = mRectF.top + outerStrokeWidth;
            leftBtnRect.bottom = mRectF.bottom - outerStrokeWidth;

            rightBtnRect.right = mRectF.right - outerStrokeWidth;
            rightBtnRect.left = rightBtnRect.right - btnWidth;
            rightBtnRect.top = mRectF.top + outerStrokeWidth;
            rightBtnRect.bottom = mRectF.bottom - outerStrokeWidth;

            centerDotRect.left = centerX - centerDotWidth * 13.5f;
            centerDotRect.right = centerDotRect.left + centerDotWidth;
            centerDotRect.top = centerY - centerDotWidth / 2;
            centerDotRect.bottom = centerY + centerDotWidth / 2;
            for (int i = 0; i < 14; i++) {
                mPaint.setColor(centerDotColor);
                canvas.drawRect(centerDotRect, mPaint);
                centerDotRect.offset(centerDotWidth * 2, 0);
            }
            mPaint.setColor(innerColor);
            if(currentState != STATE_EXTAND && selectedBtn != SELECT_BTN_NONE){
                drawHalfCircleRect(canvas, btnBgRect, btnWidth - (mRectF.bottom - mRectF.top) + outerStrokeWidth * 2 + btnExtraWidth);
            }else if (selectedBtn == SELECT_BTN_LEFT) {
                drawHalfCircleRect(canvas, leftBtnRect, btnWidth - (mRectF.bottom - mRectF.top) + outerStrokeWidth * 2);
            } else if (selectedBtn == SELECT_BTN_RIGHT) {
                drawHalfCircleRect(canvas, rightBtnRect, btnWidth - (mRectF.bottom - mRectF.top) + outerStrokeWidth * 2);

            }

            if (leftBtnBitmap == null) {
                leftBtnBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_add)).getBitmap();
            }
            canvas.drawBitmap(leftBtnBitmap, leftBtnRect.centerX() - leftBtnBitmap.getWidth() / 2, leftBtnRect.centerY() - leftBtnBitmap.getHeight() / 2, mPaint);

            if (rightBtnBitmap == null) {
                rightBtnBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.icon_delete)).getBitmap();
            }
            canvas.drawBitmap(rightBtnBitmap, rightBtnRect.centerX() - rightBtnBitmap.getWidth() / 2, rightBtnRect.centerY() - rightBtnBitmap.getHeight() / 2, mPaint);

        }
//        if(currentState == STATE_IDLE){
        mPaint.setColor(innerColor);
        float radius = getHeight() / 2 - defaultPadding - innerPaddingWidth;
        if(radius > (getHeight() / 2 - defaultPadding) / 5 * 3){
            if(innerPaddingWidth > defaultInnerPaddingWidth ){
                Shader shader = new RadialGradient(centerX,centerY,getHeight() / 2 - defaultPadding,innerColor,outerColor,RadialGradient.TileMode.CLAMP);
                mPaint.setShader(shader);
            }
            canvas.drawCircle(centerX, centerY, radius , mPaint);
            mPaint.setShader(null);
        }

        if (radius > contentPadding && innerPaddingWidth <= defaultInnerPaddingWidth) {
            mPaint.setColor(Color.WHITE);
            if(roteDegree > 0){
                canvas.rotate(roteDegree,centerX,centerY);
            }
            canvas.drawRect(centerX - radius + contentPadding,centerY - lineWidth/2, centerX + radius - contentPadding, centerY + lineWidth/2, mPaint);
            canvas.drawRect(centerX - lineWidth/2, contentPadding + defaultPadding + innerPaddingWidth, centerX + lineWidth/2, getHeight() - contentPadding - defaultPadding - innerPaddingWidth, mPaint);
         }
//        }else{
//
//        }
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        super.onMeasure(MeasureSpec.makeMeasureSpec(height + centerRectWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
//    }


    private void drawHalfCircleRect(Canvas canvas, RectF rect, float innerRectWidth) {
        if (rect.width() < innerRectWidth) {
            return;
        }
        RectF leftCircleRect = new RectF();
        RectF rightCircleRect = new RectF();

        leftCircleRect.left = rect.left;
        leftCircleRect.right = rect.left + rect.height();
        leftCircleRect.top = rect.top;
        leftCircleRect.bottom = rect.bottom;

        rightCircleRect.left = rect.right - rect.height();
        rightCircleRect.right = rect.right;
        rightCircleRect.top = rect.top;
        rightCircleRect.bottom = rect.bottom;

        Path path = new Path();
        path.moveTo(rect.left + rect.height() / 2, rect.top);
        path.moveTo(rect.left + rect.height() / 2, rect.top);
        path.lineTo(rect.right - rect.height() / 2, rect.top);
        path.arcTo(rightCircleRect, 270, 180);
        path.lineTo(rect.left + rect.height() / 2, rect.bottom);
        path.arcTo(leftCircleRect, 90, 180);
        path.close();

        canvas.drawPath(path, mPaint);
    }


    public float getOuterStrokeWidth() {
        return outerStrokeWidth;
    }

    public void setOuterStrokeWidth(float outerStrokeWidth) {
        this.outerStrokeWidth = outerStrokeWidth;
        invalidate();
    }

    public int getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
    }

    public void setSelectedBtn(int selectedBtn) {
        this.selectedBtn = selectedBtn;
    }

    public void selecteLeftBtn() {
        animChangeBtn(SELECT_BTN_LEFT);
    }

    public void selecteRightBtn() {
        animChangeBtn(SELECT_BTN_RIGHT);
    }

    public void clearSelectedBtn() {
        animChangeBtn(SELECT_BTN_NONE);
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d("onDrop");
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d("onDrop " + " " + x + "," + y + " ," + xOffset + ", " + yOffset);
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d("onDragOver " + " " + x + "," + y + " ," + xOffset + ", " + yOffset);
        if (x + xOffset > leftBtnRect.left && x+xOffset < leftBtnRect.right) {
            selecteLeftBtn();
        } else   if (x + xOffset > rightBtnRect.left && x+xOffset < rightBtnRect.right) {
            selecteRightBtn();
        } else {
            clearSelectedBtn();
        }
        MLogs.d("left:" + leftBtnRect.toString());
        MLogs.d("right:" + rightBtnRect.toString());
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d("onDragExit");
        clearSelectedBtn();
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        return true;
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }
}
