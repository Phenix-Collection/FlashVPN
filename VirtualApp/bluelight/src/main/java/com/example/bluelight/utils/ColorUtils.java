package com.example.bluelight.utils;

import android.graphics.Color;

public class ColorUtils {
    public ColorUtils() {
        super();
    }

    public static int convertColorTemperature(int arg7) {
        return Color.argb(255, Math.max(255 - Math.round((((float) (arg7 - 3500))) / 3500f * 255f), 0), Math.min(Math.round((((float) (arg7 - 1000))) / 3500f * 255f), 255), Math.min(Math.max(Math.round((((float) (arg7 - 3500))) / 3500f * 255f), 0) * 4, 255));
    }

    public static int fitToColor(int arg2) {
        return ColorUtils.fitToRange(arg2, 0, 255);
    }

    public static int fitToRange(int arg0, int arg1, int arg2) {
        return Math.min(arg2, Math.max(arg1, arg0));
    }

    public static int getBackgroundColor(int dim) {
        if (dim < 0) {
            dim = 0;
        } else if (dim > 75) {
            dim = 75;
        }

        return Color.argb(((int) ((((float) dim)) * 255f / 100f)), 0, 0, 0);
    }

    public static int getForegroundColor(int ct, int in) {
        if (in < 0) {
            in = 0;
        } else if (in > 80) {
            in = 80;
        }

        int v0 = ColorUtils.toRGB(ct);
        return Color.argb(((int) ((((float) in)) * 255f / 100f)), Color.red(v0), Color.green(v0), Color.blue(v0));
    }

    public static int toRGB(int arg10) {
        float v5;
        int v2;
        int v0 = 6500;
        if (arg10 == v0) {
            return Color.rgb(255, 255, 255);
        }

        int v3 = 0;
        float v4 = (((float) arg10)) * 0.01f;
        if (arg10 < v0) {
            v0 = 255;
            float v1 = v4 - 2f;
            v2 = ((int) (-155.254852f - 0.445969f * v1 + 104.492165f * (((float) Math.log(((double) v1))))));
            if (arg10 > 2000) {
                v5 = v4 - 10f;
                v3 = ((int) (-254.769348f + 0.82741f * v5 + 115.679947f * (((float) Math.log(((double) v5))))));
            }
        } else {
            v3 = 255;
            float v0_1 = v4 - 55f;
            int v1_1 = ((int) (351.976898f + 0.114206f * v0_1 - 40.253662f * (((float) Math.log(((double) v0_1))))));
            v5 = v4 - 50f;
            v2 = ((int) (325.449402f + 0.079435f * v5 - 28.085297f * (((float) Math.log(((double) v5))))));
            v0 = v1_1;
        }

        return Color.rgb(ColorUtils.fitToColor(v0), ColorUtils.fitToColor(v2), ColorUtils.fitToColor(v3));
    }
}

