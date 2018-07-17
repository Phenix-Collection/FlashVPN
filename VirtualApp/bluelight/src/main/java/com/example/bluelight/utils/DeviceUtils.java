package com.example.bluelight.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class DeviceUtils {
    public DeviceUtils() {
        super();
    }

    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(point);
        } else if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(point);
        } else {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }

        return point;
    }

    public static int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int id = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) {
            return res.getDimensionPixelSize(id);
        }

        return 0;
    }
}

