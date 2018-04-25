package mochat.multiple.parallel.whatsclone.utils;

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
import android.graphics.drawable.Drawable;

import mochat.multiple.parallel.whatsclone.constant.AppConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by yxx on 2016/7/22.
 */
public class BitmapUtils {

    @Deprecated
    public static Bitmap getCustomIcon(Context context, String pkg) {
        return getCustomIcon(context, pkg, 0);
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

    public static Bitmap createBadgeIcon(Context context, Drawable appIcon, int userId){
        if(appIcon == null){
            return null;
        }
        Bitmap shortCutBitMap;
        try{
            int width = DisplayUtils.dip2px(context, AppConstants.APP_ICON_WIDTH);
            int padding = DisplayUtils.dip2px(context, AppConstants.APP_ICON_PADDING);
            shortCutBitMap = Bitmap.createBitmap(width,width,Bitmap.Config.ARGB_8888);
            Bitmap mShape = BitmapFactory.decodeResource(context.getResources(), CommonUtils.getRingIconId(userId));
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

    public static Bitmap drawableToBitmap(Drawable drawable) {



        Bitmap bitmap = Bitmap.createBitmap(

                drawable.getIntrinsicWidth(),

                drawable.getIntrinsicHeight(),

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

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
        String pathname = context.getFilesDir() +  AppConstants.ICON_FILE_PATH + "/" + pkg;
        return AppManager.getCompatibleName(pathname, userId);
    }

    public static void removeCustomIcon(Context context, String pkg, int userId) {
        String path = getCustomIconPath(context, pkg, userId);
        File file = new File(path);
        file.delete();
    }
}
