package com.polestar.multiaccount.component.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.utils.CloneHelper;

/**
 * Created by yxx on 2016/8/23.
 */
public class LauncherActivity extends BaseActivity{

    private ViewGroup mainLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylauncher);
        mainLayout = (ViewGroup) findViewById(R.id.launcher_bg);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(LauncherActivity.this).preLoadClonedApp(LauncherActivity.this);
            }
        },100);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LauncherActivity.this,HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, -1);
                finish();
            }
        },1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}
