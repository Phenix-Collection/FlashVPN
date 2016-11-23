package com.polestar.multiaccount.utils;

import android.content.Context;
import android.renderscript.RenderScript;

/**
 * 某些机型上RenderScript.destroy()会抛出异常，这里只在app退出的时候销毁RenderScript
 * Created by yxx on 2016/9/8.
 */
public class RenderScriptManager {
    private static RenderScript rs;

    public static RenderScript createRenderScript(Context context){
        if(rs == null){
            rs = RenderScript.create(context);
        }
        return rs;
    }

    public static void destroy(){
        if(rs != null){
            rs.destroy();
            rs = null;
        }
    }
}
