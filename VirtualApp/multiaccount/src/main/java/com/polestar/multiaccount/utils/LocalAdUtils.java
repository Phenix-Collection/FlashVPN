package com.polestar.multiaccount.utils;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Created by yxx on 2016/8/10.
 */
public class LocalAdUtils {

    private static boolean NEED_SHOW_AD = false;
    public static final String AD_STYLE[] = {"12310", "12311", "12312", "12313", "12314", "12315", "12316", "12317", "12318", "12319",
            "12320", "12321"};

    public static void showFullScreenAd(Context context,boolean isPreLoad){
        showAd(context,null, AD_STYLE[11],0,0,false);
    }
    public static void showFullScreenAd(Context context,boolean isPreLoad,Object onAdLoadListener){
        showAd(context,null, AD_STYLE[11],0,0,false,onAdLoadListener);
    }

    public static void showAd(Context context, ViewGroup container, String style, boolean isPreLoad){
        showAd(context,container,style,0,0,isPreLoad);
    }
    public static void showAd(Context context, ViewGroup container, String style, boolean isPreLoad,Object onAdLoadListener){
        showAd(context,container,style,0,0,isPreLoad,onAdLoadListener);
    }

    public static void showAd(Context context, ViewGroup container, String style, int width, int height, boolean isPreLoad){

    }
    public static void showAd(Context context, ViewGroup container, String style, int width, int height, boolean isPreLoad,Object onAdLoadListener){
        if(!NEED_SHOW_AD){
            return;
        }
    }

}
