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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.constant.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by yxx on 2016/7/22.
 */
public class BitmapUtils {
    private static final int SCALE_FACTOR = 4;

    public static Bitmap createCustomIcon(Context context, Drawable appIcon){
        if(appIcon == null){
            return null;
        }
        Bitmap shortCutBitMap;
        try{
            int width = DisplayUtils.dip2px(context, Constants.APP_ICON_WIDTH);
            int padding = DisplayUtils.dip2px(context, Constants.APP_ICON_PADDING);
            shortCutBitMap = Bitmap.createBitmap(width,width,Bitmap.Config.ARGB_8888);
            Bitmap mShape = BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon_shape);
            Canvas canvas = new Canvas(shortCutBitMap);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            final Rect rect = new Rect(0, 0, width, width);
            final float roundPx = DisplayUtils.dip2px(context,Constants.APP_ICON_RADIUS);
            canvas.drawRoundRect(new RectF(rect),roundPx,roundPx,paint);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

            appIcon.setBounds(padding,padding,width - padding,width - padding);
            appIcon.draw(canvas);

            canvas.drawBitmap(Bitmap.createScaledBitmap(mShape,width,width,true),new Rect(0,0,width,width),new Rect(0,0,width,width),null);
        }catch (OutOfMemoryError error){
            error.printStackTrace();
            shortCutBitMap = null;
        }
        return shortCutBitMap;
    }

    public static Bitmap scaleBitmap(Bitmap src,float scaleFactorX,float scaleFactorY){
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactorX,scaleFactorY);
        Bitmap newBitmap = Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),matrix,true);
        return newBitmap;
    }

    public static Bitmap createBlurBitmapByActivity(Activity activity){
        View view  = activity.getWindow().getDecorView();
        if(!view.isDrawingCacheEnabled()){
            view.setDrawingCacheEnabled(true);
        }else{
            view.buildDrawingCache();
        }
        Bitmap map = createBlurBitmap(activity,view.getDrawingCache(),SCALE_FACTOR);
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return map;
    }

    /**
     * API 16以上使用Google提供的模糊算法
     */
    public static Bitmap createBlurBitmap(Context context,Bitmap src,float scaleFactor){
        src = scaleBitmap(src,1f / scaleFactor,1f / scaleFactor);
        float radius = 5;
        if(Build.VERSION.SDK_INT > 16){
            try{
                RenderScript rs = RenderScriptManager.createRenderScript(context);
                Allocation overlayAlloc = Allocation.createFromBitmap(rs, src);
                ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
                blur.setInput(overlayAlloc);
                blur.setRadius(radius);
                blur.forEach(overlayAlloc);
                overlayAlloc.copyTo(src);
            }catch (Exception e){
                return doBlur(src, (int) radius,false);
            }
            return src;
        }else{
            return doBlur(src, (int) radius,false);
        }
    }

    /**
     *  Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     */
    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    public static Bitmap compressBitmap(Bitmap src){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 10, out);
        ByteArrayInputStream isBm = new ByteArrayInputStream(out.toByteArray());
        return BitmapFactory.decodeStream(isBm);
    }
}
