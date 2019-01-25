package com.polestar.superclone.widgets;

/**
 * Created by guojia on 2019/1/25.
 */


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.utils.DisplayUtils;

/**
 *  If you want to assign custom value, first add to following code snippet to your .xml
 *    xmlns:ifTextView="http://schemas.android.com/apk/res/com.polestar.applab"
 *
 *    then assign value, for example : "ifTextView:bgColor="@color/intl_scanresult_item_circle_fb_bg"
 *
 *    we provide following custom attrs :
 *      ifTextView:bgColor
 *      ifTextView:strokeColor
 *      ifTextView:strokeWidth
 *
 */
public class IconFontTextView extends TextView {

    private static final String TAG = IconFontTextView.class.getSimpleName();
    public static final String DEFAULT_FONT = "polestar.ttf";
    public static final int BG_SHAPE_OVAL = 0;
    public static final int BG_SHAPE_RECT = 1;
    private final int defaultBgColor = Color.parseColor("#dc552c");
    private final int defaultStrokeColor = Color.parseColor("#00000000");
    private final float defaultStrokeWidth = 0;

    private int mShapeType;
    private int mStrokeColor;
    private float mStrokeWidth;
    private boolean mIsFlipHorizontal;
    private TextPaint mStrokePaint;

    private String mFontName;
    //Central Transparent Mode
    private boolean mCentralTransparentMode=false;
    private Bitmap mask1;
    private Bitmap mask2;
    private Bitmap mask3;
    private Bitmap mask4;

    private Canvas maskCanvas1;
    private Canvas maskCanvas2;
    private Canvas maskCanvas3;
    private Canvas maskCanvas4;
    private PorterDuffXfermode xfeMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private Paint dummyPaint = new Paint();
    private int maskBaseColor=0xFFFFFFFF;
    Paint paint2 = new Paint();
    Paint paint3 = new Paint();

    private float mIconDegrees = 0f;

    public IconFontTextView(Context context) {
        this(context, null);
    }

    public IconFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconFontTextView, defStyle, 0);

        try {

            mFontName = DEFAULT_FONT;

            mIsFlipHorizontal = a.getBoolean(R.styleable.IconFontTextView_flip_horizontal, false);

            // setup stroke -- begin
            mStrokeColor = a.getColor(R.styleable.IconFontTextView_strokeColor, defaultStrokeColor);
            mStrokeWidth = a.getFloat(R.styleable.IconFontTextView_icon_strokeWidth, defaultStrokeWidth);
            mStrokePaint = new TextPaint();

            // copy
            mStrokePaint.setTextSize(getTextSize());
            mStrokePaint.setTypeface(getTypeface());
            mStrokePaint.setFlags(getPaintFlags());

            // custom
            try {
                mStrokePaint.setStyle(Paint.Style.STROKE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStrokePaint.setColor(mStrokeColor);
            mStrokePaint.setStrokeWidth(mStrokeWidth);
            // setup stroke -- end
            mCentralTransparentMode = a.getBoolean(R.styleable.IconFontTextView_central_transparent, false);
            maskBaseColor = a.getColor(R.styleable.IconFontTextView_central_bgcolor, 0x88FFFFFF);
//            if(DebugMode.mEnableLog) DebugMode.Log("IconFontTextView", "mCentralTransparentMode " + mCentralTransparentMode);

            mShapeType = a.getInt(R.styleable.IconFontTextView_bgShape, -1);
            int bgShapeColor = a.getColor(R.styleable.IconFontTextView_bgColor, defaultBgColor);
            setBackgroundShapeDrawable(mShapeType, bgShapeColor);

            setTypeface();

//            if (DebugMode.mEnableLog) {
//                StringBuilder sb = new StringBuilder();
//                sb.append(", bgShape : ").append(mShapeType)
//                  .append(", strokeColor : ").append(mStrokeColor)
//                  .append(", strokeWidth : ").append(mStrokeWidth);
//                DebugMode.Log(TAG, "IconFontTextView attrs : " + sb.toString());
//            }

        } finally {
            a.recycle();
        }

    }

    public void setIconDegrees(float degrees) {
        mIconDegrees = degrees;
    }

    public void setBackgroundShapeDrawable(int bgShape, int bgColor) {
        if(bgShape != BG_SHAPE_OVAL && bgShape != BG_SHAPE_RECT)
            return;

        ShapeDrawable background = null;
        if (bgShape == BG_SHAPE_OVAL){
            background = new ShapeDrawable(new OvalShape());
        }else if (bgShape == BG_SHAPE_RECT){
            int r =  DisplayUtils.dip2px(MApp.getApp(), 5);
            float[] outerR = new float[] {r, r, r, r, r, r, r, r};
            background = new ShapeDrawable(new RoundRectShape(outerR, null, null));
        }

        if(background != null) {
            background.getPaint().setColor(bgColor);
            background.getPaint().setAntiAlias(true);
            this.setBackgroundDrawable(background);
        }

        mShapeType = bgShape;
    }

    public void setBackgroundColorResource(int resID){
        if (mShapeType >= 0){

            //TODO : add rect-style shape
            ShapeDrawable background = new ShapeDrawable(new OvalShape());
            background.getPaint().setColor(getResources().getColor(resID));
            background.getPaint().setAntiAlias(true);
            this.setBackgroundDrawable(background);
        }
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }


    private void setTypeface() {
        if (!TextUtils.isEmpty(mFontName)) {
            try {
                Typeface font = FontUtils.getFont(getContext(), mFontName);
                if (font != null) {
                    setTypeface(font);
                }
            } catch (Exception e) {
            }
        }
    }

    public void refreshTypeface() {
        setTypeface();
    }

    public void setCentralTransparentMode(boolean b)
    {
        mCentralTransparentMode = b;
    }
    public void setCentralTransparentBaseColor(int color)
    {
        maskBaseColor = color;
    }

    public void exitCentralTransparentMode()
    {
//        if(DebugMode.mEnableLog) DebugMode.Log("IconFontTextView", "exitCentralTransparentMode");
        safeReleaseBitmap(mask1);
        safeReleaseBitmap(mask2);
        safeReleaseBitmap(mask3);
        safeReleaseBitmap(mask4);
    }
    private void safeReleaseBitmap(Bitmap bitmap)
    {
        try
        {
            if(bitmap != null && !bitmap.isRecycled())
            {
                bitmap.recycle();
            }
        }
        catch (Exception e)
        {

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setTypeface();
        if (mIsFlipHorizontal) {
            canvas.translate(getWidth(), 0);
            canvas.scale(-1, 1);
        }
        if(mCentralTransparentMode)
        {
//            if(DebugMode.mEnableLog) DebugMode.Log("IconFontTextView", "onDraw w " + getWidth());
//            if(DebugMode.mEnableLog) DebugMode.Log("IconFontTextView", "onDraw h " + getHeight());
            if(mask1 == null || mask1.isRecycled()) {
                mask1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                maskCanvas1 = new Canvas(mask1);
            }
            if(mask2 == null || mask2.isRecycled()) {
                mask2 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                maskCanvas2 = new Canvas(mask2);
            }

            if(mask3 == null || mask3.isRecycled()) {
                mask3 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                maskCanvas3 = new Canvas(mask3);
            }

            if(mask4 == null || mask4.isRecycled()) {
                mask4 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                maskCanvas4 = new Canvas(mask4);
            }

            dummyPaint.setAntiAlias(true);
        }
        if(mCentralTransparentMode)
        {
            mask1.eraseColor(Color.TRANSPARENT);
            mask2.eraseColor(Color.TRANSPARENT);
            mask3.eraseColor(Color.TRANSPARENT);
            mask4.eraseColor(Color.TRANSPARENT);

            Paint paint =  getPaint();
            //base color transparent effect
            paint.setColor(maskBaseColor);

            //Paint paint2 = new Paint();
            paint2.set(paint);
            //paint2 = new Paint();
            paint2.setTextSize(paint.getTextSize());
            paint2.setAntiAlias(true);
            paint2.setStyle(paint.getStyle());
            paint2.setColor(0xFF000000);
            paint2.clearShadowLayer();
            paint2.setTypeface(paint.getTypeface());
            paint2.clearShadowLayer();
            // dwar normal text style
            maskCanvas1.drawText(getText().toString(), (getWidth() - getPaint().measureText(getText().toString())) / 2, getBaseline(), paint);
            //draw 50 pa
            //Paint paint3 = new Paint();
            paint3.set(paint2);
            paint2.setTextSize(paint.getTextSize());
            paint2.setAntiAlias(true);
            paint2.setStyle(paint.getStyle());
            paint2.clearShadowLayer();
            paint2.setTypeface(paint.getTypeface());
//                <color name="icon_font_textview_txt_white_50pa">#80ffffff</color>

            paint3.setColor(Color.parseColor("#80ffffff"));
            maskCanvas4.drawText(getText().toString(), (getWidth() - getPaint().measureText(getText().toString())) / 2, getBaseline(), paint3);

            //draw mask
            maskCanvas2.drawText(getText().toString(), (getWidth() - getPaint().measureText(getText().toString())) / 2, getBaseline(), paint2);
            // do dst out mode, only shadow will be remain
            maskCanvas3.drawBitmap(mask1, 0, 0, dummyPaint);
            paint2.setXfermode(xfeMode);
            maskCanvas3.drawBitmap(mask2, 0, 0, paint2);
            maskCanvas3.drawBitmap(mask4, 0, 0, dummyPaint);
            //final draw final image to view canvas
            canvas.drawBitmap(mask3, 0, 0, dummyPaint);
        }
        else {
            canvas.rotate(mIconDegrees, getWidth() / 2, getHeight() / 2);

            // Draw text stroke
            if (mStrokeWidth > 0) {
                int textColor = getTextColors().getDefaultColor();
                setTextColor(mStrokeColor); // your stroke's color
                getPaint().setStrokeWidth(mStrokeWidth);
                getPaint().setStyle(Paint.Style.STROKE);
                super.onDraw(canvas);

                // Draw original text
                setTextColor(textColor);
                getPaint().setStrokeWidth(0);
                getPaint().setStyle(Paint.Style.FILL);
            } else {
                canvas.drawText(getText().toString(), (getWidth() - mStrokePaint.measureText(getText().toString())) / 2, getBaseline(), mStrokePaint);
            }

            super.onDraw(canvas);
        }
    }
}
