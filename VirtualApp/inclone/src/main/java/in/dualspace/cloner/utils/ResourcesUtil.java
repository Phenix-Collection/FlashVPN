package in.dualspace.cloner.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.polestar.clone.BitmapUtils;
import in.dualspace.cloner.BuildConfig;
import in.dualspace.cloner.DualApp;

import java.lang.reflect.Field;


/**
 * Created by DualApp on 2017/1/1.
 */

public class ResourcesUtil {

    public static Context getContext(){
        return DualApp.getApp();
    }

    public static Resources getResources(){
        return getContext().getResources();
    }

    public static int getColor(int resId){
        return getResources().getColor(resId);
    }

    public static String getString(int resId){
        return getResources().getString(resId);
    }

    public static String getString(String paramString) {
        return getString(getStringId(paramString));
    }

    public static final int getStyleableId(Context context, String type, String name) {
        try {
            Field fieldId = Class.forName(context.getPackageName() + ".R$styleable").getField(name);
            int id = (Integer) fieldId.get(null);
            return id;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return 0;
    }

    public static final int[] getStyleableId(Context context, String name) {
        try {
            Class claz = Class.forName(context.getPackageName() + ".R$styleable");
            Field field = Class.forName(context.getPackageName() + ".R$styleable").getField(name);
            if (null != field) {
                return (int[]) field.get(null);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public static int getDrawableId(String paramString) {
        int id = getResources().getIdentifier(paramString, "drawable", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getId(String paramString) {
        int id = getResources().getIdentifier(paramString, "id", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getLayoutId(String paramString) {
        int id = getResources().getIdentifier(paramString, "layout", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getColorId(Context mContext, String paramString) {
        int id = mContext.getResources()
                .getIdentifier(paramString, "color",BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getStringId(String paramString) {
        int id = getContext().getResources()
                .getIdentifier(paramString, "string", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getAttrId(Context mContext, String paramString) {
        int id = mContext.getResources()
                .getIdentifier(paramString, "attr", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static int getDrawableId(Context mContext, String paramString) {
        int id = mContext.getResources()
                .getIdentifier(paramString, "drawable", BuildConfig.APPLICATION_ID);
        return id;
    }

    public static Bitmap getBitmap(String paramString) {
        return BitmapFactory.decodeResource(getContext().getResources(),
                getDrawableId(getContext(), paramString));
    }

    public static Drawable getDrawable(int resId){
        return getResources().getDrawable(resId);
    }

    public static Bitmap getBitmap(int resId){
        return BitmapUtils.drawableToBitmap( getDrawable(resId));
    }

}