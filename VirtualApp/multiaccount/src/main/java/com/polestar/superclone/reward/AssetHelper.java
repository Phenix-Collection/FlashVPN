package com.polestar.superclone.reward;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.polestar.ad.AdLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AssetHelper {

    private static HashMap<String, Drawable> sCache = new HashMap<>();

    public static Drawable getDrawable(Context context, String url) {
        Drawable ret = sCache.get(url);
        if (ret == null) {
            ret = getDrawableFromAssets(context, url);
            sCache.put(url, ret);
        }
        return ret;
    }

    private static Drawable getDrawableFromAssets(Context context, String url){
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(url);
            drawable = Drawable.createFromStream(inputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
            AdLog.e("Failed to load " + url + " from asset manager " + e.toString());
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return drawable;
    }
}
