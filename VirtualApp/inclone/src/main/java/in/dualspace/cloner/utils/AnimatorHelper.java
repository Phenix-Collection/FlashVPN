package in.dualspace.cloner.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by DualApp on 2016/8/22.
 */
public class AnimatorHelper {

    public static final int DURATION_NORMAL = 500;
    public static final int DURATION_SHORT = 300;
    public static final int DURATION_LONG = 500;

    public static void elasticScale(View view){
        ViewHelper.setScaleX(view,1.02f);
        ViewHelper.setScaleY(view,1.02f);
        ViewPropertyAnimator.animate(view).setInterpolator(new AnticipateOvershootInterpolator(12f))
                .setDuration(DURATION_NORMAL).scaleX(1f).scaleY(1f);
    }

    public static void verticalShowFromBottom(View view){
        int translationY = view.getHeight() + getMarginBottom(view);
        ViewHelper.setTranslationY(view, translationY);
        ViewPropertyAnimator.animate(view)
                .setDuration(DURATION_NORMAL)
                .translationY(0);
    }


    public static void hideToBottom(View view){
        int translationY = view.getHeight() + getMarginBottom(view);
        ViewHelper.setTranslationY(view, translationY);
    }


    public static void fadeOut(View view){
        ViewHelper.setAlpha(view,1.0f);
        ViewPropertyAnimator.animate(view).setDuration(DURATION_LONG)
                .alpha(0);
    }

    private static int getMarginBottom(View view) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }
    private static int getMarginTop(View view) {
        int marginTop = 0;
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginTop = ((ViewGroup.MarginLayoutParams) layoutParams).topMargin;
        }
        return marginTop;
    }
}
