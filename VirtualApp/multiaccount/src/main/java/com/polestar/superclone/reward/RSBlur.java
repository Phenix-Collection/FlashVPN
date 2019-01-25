package com.polestar.superclone.reward;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Created by guojia on 2019/1/24.
 */
public class RSBlur {
    public RSBlur() {
        super();
    }

    @TargetApi(value=17) public static Bitmap blur(Context arg7, Bitmap arg8, int arg9) throws RSRuntimeException {
        RenderScript v2 = null;
        try {
            v2 = RenderScript.create(arg7);
            Allocation v3 = Allocation.createFromBitmap(v2, arg8, Allocation.MipmapControl.MIPMAP_NONE, 1);
            Allocation v4 = Allocation.createTyped(v2, v3.getType());
            ScriptIntrinsicBlur v5 = ScriptIntrinsicBlur.create(v2, Element.U8_4(v2));
            v5.setInput(v3);
            v5.setRadius(((float)arg9));
            v5.forEach(v4);
            v4.copyTo(arg8);
            if(v2 == null) {
                return arg8;
            }
        }
        catch(Throwable v6) {
            if(v2 != null) {
                v2.destroy();
            }

            throw v6;
        }

        v2.destroy();
        return arg8;
    }
}