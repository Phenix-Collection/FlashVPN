package com.polestar.multiaccount.component.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.PreferencesUtils;

/**
 * Created by yxx on 2016/8/23.
 */
public class LauncherActivity extends BaseActivity{

    private static boolean created;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylauncher);
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
        if(!PreferencesUtils.isShortCutCreated() && !created) {
            PreferencesUtils.setShortCutCreated();
            createShortCut();
            created = true;
        }
    }
    public void createShortCut(){
        //创建快捷方式的Intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        //需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext() , LauncherActivity.class));
        //发送广播。OK
        sendBroadcast(shortcutintent);
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
