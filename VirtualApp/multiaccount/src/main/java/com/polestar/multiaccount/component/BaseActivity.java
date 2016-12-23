package com.polestar.multiaccount.component;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.polestar.multiaccount.R;


/**
 * Created by yxx on 2016/7/14.
 */
public class BaseActivity extends AppCompatActivity {

    private TextView titleTv;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if(useCustomTitleBar()){
            View contentView = LayoutInflater.from(this).inflate(layoutResID,null);
            super.setContentView(addContentView(contentView));
        }else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if(useCustomTitleBar()){
            super.setContentView(addContentView(view));
        }else{
            super.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if(useCustomTitleBar()){
            super.setContentView(addContentView(view),params);
        }else{
            super.setContentView(view, params);
        }
    }

    private View addContentView(View contentView){
        View customLayout = LayoutInflater.from(this).inflate(R.layout.common_layout,null);
        FrameLayout contentLayout = (FrameLayout) customLayout.findViewById(R.id.content_home);
        titleTv = (TextView) customLayout.findViewById(R.id.title);
        contentLayout.addView(contentView);
        return customLayout;
    }

    /**
     * 设置标题
     */
    public void setTitle(String title){
        if(titleTv != null){
            titleTv.setText( title);
        }
    }

    /**
     * 返回按钮
     */
    public void onNavigationClick(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        try{
            super.onBackPressed();
        }catch (Exception e){
            finish();
        }
    }

    /**
     * 子类如果不需要添加titleBar，可重写该方法 return false;
     */
    protected boolean useCustomTitleBar(){
        return true;
    }
}
