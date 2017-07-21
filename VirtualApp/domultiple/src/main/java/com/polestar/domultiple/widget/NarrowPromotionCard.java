package com.polestar.domultiple.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polestar.domultiple.R;

/**
 * Created by guojia on 2017/7/20.
 */

public class NarrowPromotionCard extends LinearLayout implements View.OnClickListener{
    private Intent mAction;

    public NarrowPromotionCard(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.narrow_promotion_card, this, true);
        this.setOnClickListener(this);
    }

    public NarrowPromotionCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.narrow_promotion_card, this, true);
        this.setOnClickListener(this);
    }

    public NarrowPromotionCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.narrow_promotion_card, this, true);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mAction != null) {
            getContext().startActivity(mAction);
        }
    }

    public void init (int iconId, int titleId, Intent action) {
        ImageView iv = (ImageView) findViewById(R.id.icon);
        TextView title = (TextView) findViewById(R.id.title);
        iv.setImageResource(iconId);
        title.setText(titleId);
        this.mAction = action;
    }
}
