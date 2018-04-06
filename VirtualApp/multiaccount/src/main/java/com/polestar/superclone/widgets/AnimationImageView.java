package com.polestar.superclone.widgets;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yxx on 2016/8/26.
 */
public class AnimationImageView extends ImageView {

    private AnimationDrawable animationDrawable;
    public AnimationImageView(Context context) {
        super(context);
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init(){
        if(getDrawable() != null && getDrawable() instanceof  AnimationDrawable)
            animationDrawable = (AnimationDrawable) getDrawable();
    }

    public void playOnce(){
        init();
        if(animationDrawable != null){
            animationDrawable.setOneShot(true);
            animationDrawable.start();
        }
    }

    public void stop(){
        init();
        animationDrawable.stop();
    }

}
