package com.polestar.multiaccount.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.constant.AppConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by yxx on 2016/7/22.
 */
public class BitmapUtils {

    public static Bitmap createCustomIcon(Context context, Drawable appIcon){
        if(appIcon == null){
            return null;
        }
        Bitmap shortCutBitMap;
        try{
            int width = DisplayUtils.dip2px(context, AppConstants.APP_ICON_WIDTH);
            int padding = DisplayUtils.dip2px(context, AppConstants.APP_ICON_PADDING);
            shortCutBitMap = Bitmap.createBitmap(width,width,Bitmap.Config.ARGB_8888);
            Bitmap mShape = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ring_icon);
            Canvas canvas = new Canvas(shortCutBitMap);

            Paint paint = new Paint();
            paint.setColor(Color.TRANSPARENT);
            final Rect rect = new Rect(0, 0, width, width);
            final float roundPx = DisplayUtils.dip2px(context, AppConstants.APP_ICON_RADIUS);
            canvas.drawRoundRect(new RectF(rect),roundPx,roundPx,paint);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);

            appIcon.setBounds(padding,padding,width - padding,width - padding);
            appIcon.draw(canvas);

            canvas.drawBitmap(Bitmap.createScaledBitmap(mShape,width,width,true),new Rect(0,0,width,width),new Rect(0,0,width,width),null);
        }catch (OutOfMemoryError error){
            error.printStackTrace();
            shortCutBitMap = null;
        }
        return shortCutBitMap;
    }
}
