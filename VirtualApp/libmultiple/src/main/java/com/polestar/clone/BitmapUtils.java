package com.polestar.clone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.polestar.clone.client.core.VirtualCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Lody
 */
public class BitmapUtils {

    public static  int APP_ICON_WIDTH = 50;//dp
    public static  int APP_ICON_RADIUS = 12;//dp
    public static  int APP_ICON_PADDING = 5;//dp
    public static final float APP_BADGE_TEXT_SIZE = 5.5f;//dp
    public static final float APP_BADGE_TEXT_SIZE_SMALL = 4.5f;//dp
    public static final String ICON_FILE_PATH = "/icons";

    public static int dip2px(Context context, float dip) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int px2dip(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            return bitmapDrawable.getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public static void saveBitmapToPNG(Bitmap bitmap,String path) throws IOException
    {
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 90, out))
            {
                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String getCustomIconPath(Context context, String pkg, int userId) {
        String pathname = context.getFilesDir() +  ICON_FILE_PATH + "/" + pkg;
        return VirtualCore.getCompatibleName(pathname, userId);
    }

    public static void removeCustomIcon(Context context, String pkg, int userId) {
        String path = getCustomIconPath(context, pkg, userId);
        File file = new File(path);
        file.delete();
    }

    public static Bitmap getCustomIcon(Context context, String pkg, int userId) {
        String pathname = getCustomIconPath(context, pkg, userId);
        File file = new File(pathname);
        Bitmap bmp = null;
        if (file.exists()) {
            bmp = BitmapFactory.decodeFile(file.getPath());
        }
        if (bmp != null) {
            return  bmp;
        }
        try {
            Drawable defaultIcon = context.getPackageManager().getApplicationIcon(pkg);
            return createBadgeIcon(context, defaultIcon, userId);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getRingIconId(Context paramContext, int userId) {
        return paramContext.getResources().getIdentifier(userId == 0? "ring_icon" : "ring_icon_template",
                "mipmap", paramContext.getPackageName());
    }

    public static Bitmap createBadgeIcon(Context context, Drawable appIcon, int userId){
        if(appIcon == null){
            return null;
        }
        Bitmap shortCutBitMap;
        try{
            int width = dip2px(context, APP_ICON_WIDTH);
            int padding = dip2px(context, APP_ICON_PADDING);
            shortCutBitMap = Bitmap.createBitmap(width,width,Bitmap.Config.ARGB_8888);
            Bitmap mShape = BitmapFactory.decodeResource(context.getResources(), getRingIconId(context, userId));
            Canvas canvas = new Canvas(shortCutBitMap);

            Paint paint = new Paint();
            paint.setColor(Color.TRANSPARENT);
            final Rect rect = new Rect(0, 0, width, width);
            final float roundPx = dip2px(context, APP_ICON_RADIUS);
            canvas.drawRoundRect(new RectF(rect),roundPx,roundPx,paint);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);

            appIcon.setBounds(padding,padding,width - padding,width - padding);
            appIcon.draw(canvas);

            //Bitmap numberBmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_number_1);
            canvas.drawBitmap(Bitmap.createScaledBitmap(mShape,width,width,true),new Rect(0,0,width,width),new Rect(0,0,width,width),null);
//            int numberWidth = DisplayUtils.dip2px(context, 10);
//            canvas.drawBitmap(Bitmap.createScaledBitmap(numberBmp,numberWidth,numberWidth,true),
//                    new Rect(0,width-numberWidth,numberWidth,width), new Rect(0,width-numberWidth,numberWidth,width),null);
             if(context.getPackageName().startsWith("do.multiple.cloner")
                     || context.getPackageName().startsWith("in.dualspace.cloner")) {
                if (userId >= 1) {
                    paint.setColor(Color.parseColor("#FFFFFF"));
                    paint.setAntiAlias(true);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    if (userId < 9) {
                        paint.setTextSize(dip2px(context, APP_BADGE_TEXT_SIZE));
                        canvas.drawText("" + (userId + 1),
                                 dip2px(context, 6), width - dip2px(context, 5.3f), paint);
                    } else {
                        paint.setTextSize(dip2px(context, APP_BADGE_TEXT_SIZE_SMALL));
                        canvas.drawText("" + (userId + 1),
                                 dip2px(context, 5.0f), width - dip2px(context, 6f), paint);
                    }
                }
            } else if(context.getPackageName().startsWith("com.polestar.super.clone"))  {
                     if (userId >= 1) {
                         paint.setColor(Color.parseColor("#FFFFFF"));
                         paint.setAntiAlias(true);
                         paint.setTypeface(Typeface.DEFAULT_BOLD);
                         if (userId < 9) {
                             paint.setTextSize(dip2px(context, APP_BADGE_TEXT_SIZE));
                             canvas.drawText("" + (userId + 1),
                                     width - dip2px(context, 6), width - dip2px(context, 5), paint);
                         } else {
                             paint.setTextSize(dip2px(context, APP_BADGE_TEXT_SIZE_SMALL));
                             canvas.drawText("" + (userId + 1),
                                     width - dip2px(context, 7.3f), width - dip2px(context, 5.6f), paint);
                         }
                     }
             } else {
                 if (userId >= 1) {
                     paint.setColor(Color.parseColor("#FFFFFF"));
                     paint.setAntiAlias(true);
                     paint.setTypeface(Typeface.DEFAULT_BOLD);
                     if (userId < 9) {
                         paint.setTextSize(dip2px(context, 10.5f));
                         canvas.drawText("" + (userId + 1),
                                 width - dip2px(context, 9.5f), width - dip2px(context, 4f), paint);
                     } else {
                         paint.setTextSize(dip2px(context, 9.5f));
                         canvas.drawText("" + (userId + 1),
                                 width - dip2px(context, 13.0f), width - dip2px(context, 4f), paint);
                     }
                 }
             }
//            final Rect rect2 = new Rect(width - 40, width - 40, width, width);
//            final float roundPx2 = dip2px(context, 2);
//            canvas.drawRoundRect(new RectF(rect2),roundPx2,roundPx2,paint);
        }catch (OutOfMemoryError error){
            error.printStackTrace();
            shortCutBitMap = null;
        }
        return shortCutBitMap;
    }

    /**
     *
     * @param bm 图像 （不可修改）
     * @param hue 色相
     * @param saturation 饱和度
     * @param lum 亮度
     * @return
     */
    public static Bitmap handleImageEffect(Bitmap bm, float hue, float saturation, float lum) {

        Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix hueMatrix = new ColorMatrix();
        hueMatrix.setRotate(0, hue); // R
        hueMatrix.setRotate(1, hue); // G
        hueMatrix.setRotate(2, hue); // B

        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        ColorMatrix lumMatrix = new ColorMatrix();
        lumMatrix.setScale(lum, lum, lum, 1);

        //融合
        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(hueMatrix);
        imageMatrix.postConcat(saturationMatrix);
        imageMatrix.postConcat(lumMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        canvas.drawBitmap(bm, 0, 0, paint);

        return bmp;
    }

}
