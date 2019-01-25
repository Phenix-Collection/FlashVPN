package com.polestar.superclone.reward;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;

/**
 * Created by guojia on 2019/1/24.
 */
public class BlurTransformation {
    private static int DEFAULT_DOWN_SAMPLING;
    private static int MAX_RADIUS;
    private Context mContext;
    private int mRadius;
    private int mSampling;

    static {
        BlurTransformation.MAX_RADIUS = 25;
        BlurTransformation.DEFAULT_DOWN_SAMPLING = 1;
    }

    public BlurTransformation(Context arg3) {
        this(arg3, BlurTransformation.MAX_RADIUS, BlurTransformation.DEFAULT_DOWN_SAMPLING);
    }

    public BlurTransformation(Context arg2, int arg3, int arg4) {
        super();
        this.mContext = arg2.getApplicationContext();
        this.mRadius = arg3;
        this.mSampling = arg4;
    }

    public BlurTransformation(Context arg2, int arg3) {
        this(arg2, arg3, BlurTransformation.DEFAULT_DOWN_SAMPLING);
    }

    public String key() {
        return "BlurTransformation(radius=" + this.mRadius + ", sampling=" + this.mSampling + ")";
    }

    public Bitmap transform(Bitmap arg10) {
        Bitmap v5 = Bitmap.createBitmap(arg10.getWidth() / this.mSampling, arg10.getHeight() / this.mSampling, Bitmap.Config.ARGB_8888);
        Canvas v6 = new Canvas(v5);
        v6.scale(1f / (((float)this.mSampling)), 1f / (((float)this.mSampling)));
        Paint v7 = new Paint();
        v7.setFlags(2);
        v6.drawBitmap(arg10, 0f, 0f, v7);
        if(Build.VERSION.SDK_INT >= 17) {
            try {
                v5 = RSBlur.blur(this.mContext, v5, this.mRadius);
            }
            catch(RSRuntimeException v8) {
                v5 = FastBlur.blur(v5, this.mRadius, true);
            }
        }
        else {
            v5 = FastBlur.blur(v5, this.mRadius, true);
        }

        arg10.recycle();
        return v5;
    }
}
