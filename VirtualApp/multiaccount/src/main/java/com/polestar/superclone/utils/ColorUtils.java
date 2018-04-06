package com.polestar.superclone.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.polestar.superclone.MApp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guojia on 2017/7/23.
 */

public class ColorUtils {
    private final static String TAG = "ColorUtil";
    private final static boolean DEBUG = false;

    private final static String SUFFIX = "_color";

    public static final int DEFAULT_COLOR = 0xff357cd5;

    private static HashMap<Integer, Integer> STD_COLORS = new HashMap<Integer, Integer>();

    private static HashMap<String, Integer> PREDEFINED_COLORS = new HashMap<String, Integer>();

    public static int getIconMainColor(String app, Drawable icon) {
        long t = System.currentTimeMillis();

        if (PREDEFINED_COLORS.containsKey(app)) {
            return PREDEFINED_COLORS.get(app);
        }
        int color = PreferencesUtils.getInt(MApp.getApp(), app + SUFFIX, Color.TRANSPARENT);
        if (color != Color.TRANSPARENT) {
            return color;
        }

        Bitmap bitmap = convertToBitmap(icon);
        if (bitmap == null) {
            return 0;
        }
        if (STD_COLORS.size() == 0) {
            initStandardColors();
        }
        if (PREDEFINED_COLORS.size() == 0) {
            initPredefinedColors();
        }

        HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();
        Integer count;
        for (int x = 0; x < bitmap.getWidth(); x += 3) {
            for (int y = 0; y < bitmap.getHeight(); y += 3) {
                color = bitmap.getPixel(x, y);
                color = normalizeColor(color);
                if (color == Color.TRANSPARENT) {
                    continue;
                }

                count = colors.get(color);
                if (count == null) {
                    count = 1;
                } else {
                    ++count;
                }
                colors.put(color, count);
            }
        }

        if (colors.size() == 0) {
            if (DEBUG) Log.d(TAG, "no valid color, use default");
            PreferencesUtils.putInt(MApp.getApp(), app + SUFFIX, DEFAULT_COLOR);
            return DEFAULT_COLOR;
        }

        int max = Collections.max(colors.values());
        for (Map.Entry<Integer, Integer> entry : colors.entrySet()) {
            if (DEBUG) {
                Log.d(TAG, "color=" + colorToString(entry.getKey()) + " count=" + entry.getValue());
            }
            if (entry.getValue().equals(max)) {
                color = entry.getKey();
            }
        }
        PreferencesUtils.putInt(MApp.getApp(),app + SUFFIX, color);
        if (DEBUG) {
            Log.d(TAG, "getIconMainColor elapsed time = " + (System.currentTimeMillis() - t) + "ms");
        }
        return color;
    }

    public static int getDarkerColor(int color) {
        float hsv[] = {0, 0, 0};
        Color.colorToHSV(color, hsv);
        hsv[2] -= 0.3f;
        return Color.HSVToColor(hsv);
    }

    private static Bitmap convertToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap
                .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static int normalizeColor(int color) {
        color = blendWhite(color);
        float hsv[] = {0, 0, 0};
        Color.colorToHSV(color, hsv);

        if (!isValidColor(hsv)) {
            return Color.TRANSPARENT;
        }

        int hue = (((int) hsv[0] + 8) / 16) * 16 - 8;
        if (hue < 0) {
            hue = 0;
        }

        if (STD_COLORS.containsKey(hue)) {
            return STD_COLORS.get(hue);
        }

        hsv[0] = hue;
        hsv[1] = 0.75f;
        hsv[2] = 1.0f;
        return Color.HSVToColor(hsv);
    }

    private static int blendWhite(int color) {
        float alpha = Color.alpha(color) / 255;
        return Color.rgb((int) (Color.red(color) * alpha), (int) (Color.green(color) * alpha),
                (int) (Color.blue(color) * alpha));
    }

    private static boolean isValidColor(float hsv[]) {
        if (hsv[1] < 0.15f || hsv[2] < 0.15f) {
            return false;
        }
        return true;
    }

    private static void initStandardColors() {
        STD_COLORS.put(0, 0xffFF5959);
        STD_COLORS.put(56, 0xffFFDD00);
    }

    private static void initPredefinedColors() {
        PREDEFINED_COLORS.put("com.android.chrome", 0xffFFDD00);
        PREDEFINED_COLORS.put("com.whatsapp", 0xff40FF40); // std hue = 120
        PREDEFINED_COLORS.put("com.facebook.katana", 0xff408CFF); // std hue = 216
        PREDEFINED_COLORS.put("com.facebook.orca", 0xff40BFFF); // std hue = 200
        PREDEFINED_COLORS.put("jp.naver.line.android", 0xffA6FF40); // std hue = 88
        PREDEFINED_COLORS.put("com.twitter.android", 0xff40BFFF); // std hue = 200
        PREDEFINED_COLORS.put("com.google.android.youtube", 0xffFF5959); // c hue = 0
        PREDEFINED_COLORS.put("com.dropbox.android", 0xff40BFFF); // std hue = 200
        PREDEFINED_COLORS.put("com.skype.raider", 0xff40BFFF); // std hue = 200
        PREDEFINED_COLORS.put("com.tencent.mm", 0xff40FF40); // std hue = 120
        PREDEFINED_COLORS.put("com.evernote", 0xff40FF40); // std hue = 120
        PREDEFINED_COLORS.put("com.instagram.android", 0xffFF5940); // std hue = 8
        PREDEFINED_COLORS.put("com.kakao.talk", 0xffFFDD00); // c hue = 56
        PREDEFINED_COLORS.put("com.google.android.gm", 0xffFF5959); // c hue = 0

        PREDEFINED_COLORS.put("com.google.android.apps.docs", 0xff408CFF); // std hue = 216
        PREDEFINED_COLORS.put("com.google.android.apps.maps", 0xffB2FF59);
        PREDEFINED_COLORS.put("com.google.android.apps.plus", 0xffFF5959); // c hue = 0
        PREDEFINED_COLORS.put("com.android.packageinstaller", 0xffa0ce65); // c hue = 0

    }

    private static String colorToString(int color) {
        float hsv[] = {0, 0, 0};
        Color.colorToHSV(color, hsv);
        return "HUE=" + hsv[0] + " RGB=" + Color.red(color) + ", " + Color.green(color) + ", "
                + Color.blue(color);
    }

    public static int getLightLightColor(int color) {
        float hsv[] = {0, 0, 0};
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] * 0.75f;
        return Color.HSVToColor(hsv);
    }

    public static int getLightDarkerColor(int color) {
        float hsv[] = {0, 0, 0};
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] * 0.75f;
        return Color.HSVToColor(hsv);
    }
}
