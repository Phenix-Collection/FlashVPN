package com.polestar.superclone.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;

import java.util.Hashtable;

import android.support.annotation.IntDef;

public class FontUtils {

    private static Hashtable<String, Typeface> sFontCache = new Hashtable<String, Typeface>();

    public static Typeface getFont(Context context, String name) {
        Typeface tf = null;
//    	if ( !Commons.isTiVi() ) {
        tf = sFontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
            }
            catch (Exception e) {
                return null;
            }
            if (tf != null) {
                sFontCache.put(name, tf);
            }
        }
//    	}
        if ( tf == null ) {
            tf = Typeface.DEFAULT;
        }
        return tf;
    }

    public static final int REGULAR = 0;
    public static final int LIGHT = 1;
    public static final int CONDENSE = 2;
    public static final int THIN = 3;
    public static final int FALLBACK = 99;

    @IntDef({REGULAR, LIGHT, CONDENSE, THIN, FALLBACK})
    public @interface RobotoFamily {

    }

    public static Typeface getRobotoTypeFace(@RobotoFamily int robotoFamily) {
        return getRobotoTypeFace(robotoFamily, Typeface.NORMAL);
    }

    public static Typeface getRobotoTypeFace(@RobotoFamily int robotoFamily, int textStyle) {

        Typeface tf;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return null;
        } else {

            String fontFamilyName = null;

            switch (robotoFamily) {
                case REGULAR:
                    fontFamilyName = "sans-serif";
                    break;
                case THIN:
                    fontFamilyName = "sans-serif-light";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // Roboto thin only available above 4.2, else fallback to roboto light.
                        break;
                    }
                case LIGHT:
                    fontFamilyName = "sans-serif-light";
                    break;
                case CONDENSE:
                    fontFamilyName = "sans-serif-condensed";
                    break;
                case FALLBACK:
                default:
                    fontFamilyName = "fallback";
            }

            return Typeface.create(fontFamilyName, textStyle);
        }
    }

}
