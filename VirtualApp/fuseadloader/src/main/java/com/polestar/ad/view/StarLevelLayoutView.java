package com.polestar.ad.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.polestar.ad.R;


/**
 * Created by doriscoco on 2017/3/15.
 */

public class StarLevelLayoutView extends LinearLayout {

    public StarLevelLayoutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarLevelLayoutView(Context context) {
        this(context, null);
    }

    @SuppressLint("NewApi")
    public StarLevelLayoutView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setRating(int rating) {
        int tempRating = rating;
        if (tempRating == 0) {
            tempRating = 5;
        }
        int i = 0;
        while (i < 5) {
            ImageView imageView = new ImageView(getContext());
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            if (i < tempRating) {
                imageView.setImageResource(R.drawable.star_sel);
            } else {
                imageView.setImageResource(R.drawable.star_nor);
            }
            addView(imageView, lp);
            i++;
        }
    }

}
