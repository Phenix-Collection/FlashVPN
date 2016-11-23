package com.polestar.multiaccount.widgets;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.DisplayUtils;

/**
 * Created by yxx on 2016/8/26.
 */
public class GuideForLongPressPopWindow extends PopupWindow{
    private Context mContext;
    private int locationX,locationY;
    private int doublicationPadding;

    public GuideForLongPressPopWindow(Activity context, int locationX, int locationY){
        this.mContext = context;
        doublicationPadding = DisplayUtils.dip2px(context,15);
        this.locationX = locationX - doublicationPadding;
        this.locationY = locationY - doublicationPadding - DisplayUtils.getStatusBarHeight(context);
        initParams();
        initView();
    }

    public void show(View parent){
        try{
            showAtLocation(parent, Gravity.TOP,0,0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView(){
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_guide_for_long_press,null);
        ImageView guideImg = (ImageView) contentView.findViewById(R.id.guide_img);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) guideImg.getLayoutParams();
        layoutParams.setMargins(locationX,locationY,0,0);
//        guideImg.setPadding(locationX,locationY,0,0);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuideForLongPressPopWindow.this.dismiss();
            }
        });
        setContentView(contentView);
    }

    private void initParams() {
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.color.black));
        getBackground().setAlpha(140);
        setAnimationStyle(android.R.anim.fade_in);
    }
}
