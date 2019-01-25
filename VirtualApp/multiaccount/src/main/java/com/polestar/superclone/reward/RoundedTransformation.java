package com.polestar.superclone.reward;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Created by guojia on 2019/1/24.
 */

public class RoundedTransformation {
    private final int radius;

    public RoundedTransformation(int arg1) {
        super();
        this.radius = arg1;
    }

    public String key() {
        return "rounded";
    }

    public Bitmap transform(Bitmap arg9) {
        Paint v5 = new Paint();
        v5.setAntiAlias(true);
        v5.setShader(new BitmapShader(arg9, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        Bitmap v6 = Bitmap.createBitmap(arg9.getWidth(), arg9.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(v6).drawRoundRect(new RectF(0f, 0f, ((float)arg9.getWidth()), ((float)arg9.getHeight())), ((float)this.radius), ((float)this.radius), v5);
        if(arg9 != v6) {
            arg9.recycle();
        }

        return v6;
    }
}