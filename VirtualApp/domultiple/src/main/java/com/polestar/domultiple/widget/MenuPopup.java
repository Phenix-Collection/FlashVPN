package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;

import java.util.List;

/**
 * Created by PolestarApp on 2017/1/2.
 */
public class MenuPopup {
    private static PopupWindow popupWindow;

    private static float dip2Pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            1, PolestarApp.getApp().getResources().getDisplayMetrics());

    public static void show(View basedView, List<MenuPopItem> items){
        popupWindow = new PopupWindow(initView(basedView.getContext(), items),(int) (205*dip2Pixel), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(basedView, -(int) (170 * dip2Pixel), 0);
    }

    public static void show(View basedView, int xoff,List<MenuPopItem> items){
        popupWindow = new PopupWindow(initView(basedView.getContext(), items),
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(basedView, xoff, 0);
    }

    public static void dismiss(){
        if (popupWindow!=null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private static View initView(Context context, List<MenuPopItem> items) {
        ViewGroup vg = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.popup_menu_item, null);
        for(int i = 0; i<items.size(); i++){
            TextView tv = createTextView(context, items.get(i));
            vg.addView(tv);
        }
        return vg;
    }

    private static TextView createTextView(Context context, MenuPopItem item) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * dip2Pixel)));
        tv.setId(item.id);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding((int)(16*dip2Pixel), 0, 0, 0);
        tv.setTextColor(Color.parseColor("#666666"));
        tv.setBackgroundResource(R.drawable.selector_install_cancel);
        tv.setTextSize(14);
        tv.setSingleLine(true);
        tv.setText(item.textString);
        tv.setOnClickListener(item.clickListener);
        return tv;
    }


    public static class MenuPopItem{
        private int id;
        private String textString;
        private View.OnClickListener clickListener;

        public MenuPopItem(int id, String textString,
                           View.OnClickListener clickListener) {
            super();
            this.id = id;
            this.textString = textString;
            this.clickListener = clickListener;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTextString() {
            return textString;
        }
        public void setTextString(String textString) {
            this.textString = textString;
        }
        public View.OnClickListener getClickListener() {
            return clickListener;
        }
        public void setClickListener(View.OnClickListener clickListener) {
            this.clickListener = clickListener;
        }



    }
}
