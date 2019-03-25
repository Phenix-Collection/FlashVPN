package winterfell.flash.vpn.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import winterfell.flash.vpn.R;


/**
 * Created by PolestarApp on 2016/7/14.
 */
public class BaseActivity extends AppCompatActivity {

    private TextView titleTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImmerseLayout();
    }

    protected void setImmerseLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

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
        View bar = customLayout.findViewById(R.id.title_bar_layout);
        setImmerseLayout(bar);
        return customLayout;
    }

    protected void setImmerseLayout(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
////            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            int statusBarHeight = getStatusBarHeight(this.getBaseContext());
//            view.setPadding(0, statusBarHeight, 0, 0);
//        }
    }

    private int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
