package com.polestar.multiaccount.component.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.lody.virtual.client.core.VirtualCore;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.fragment.HomeFragment;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yxx on 2016/8/23.
 */
public class LauncherActivity extends BaseActivity{

    private static boolean created;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time = System.currentTimeMillis();
        setContentView(R.layout.activity_mylauncher);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        if (RemoteConfig.getBoolean(RemoteConfig.CONFIG_USE_MV_HOME_NATIVE)) {
            mvPreloadHomeNative();
        } else {
            FuseAdLoader.get(HomeFragment.SLOT_HOME_HEADER_NATIVE, this).loadAd(1, null);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(LauncherActivity.this).preLoadClonedApp(LauncherActivity.this);
            }
        },100);

        //VirtualCore.get().waitForEngine();
        long delta = System.currentTimeMillis() - time;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LauncherActivity.this,HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, -1);
                finish();
            }
        },2000 - delta);
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

    public void mvPreloadHomeNative() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, Object> preloadMap = new HashMap<String, Object>();
        preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);
        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, HomeFragment.UNIT_ID);
        List<MvNativeHandler.Template> list = new ArrayList<MvNativeHandler.Template>();
        list.add(new MvNativeHandler.Template(MobVistaConstans.TEMPLATE_BIG_IMG, 1));
        preloadMap.put(MobVistaConstans.NATIVE_INFO, MvNativeHandler.getTemplateString(list));
        sdk.preload(preloadMap);
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
