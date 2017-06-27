package com.polestar.multiaccount.component.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.fragment.HomeFragment;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.widgets.UpDownDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yxx on 2016/8/23.
 */
public class LauncherActivity extends BaseActivity{

    private static boolean created;
    private TextView termText ;
    private TextView enterText;
    private LinearLayout gmsSettingLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long time = System.currentTimeMillis();
        setContentView(R.layout.activity_mylauncher);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        termText = (TextView) findViewById(R.id.term_text);
        enterText = (TextView) findViewById(R.id.enter_text);
        gmsSettingLayout = (LinearLayout) findViewById(R.id.gms_setting_layout);
        FuseAdLoader.get(HomeFragment.SLOT_HOME_HEADER_NATIVE, this).loadAd(1, null);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(LauncherActivity.this).preLoadClonedApp(LauncherActivity.this);
            }
        },100);

        //VirtualCore.get().waitForEngine();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceManagerNative.ensureServerStarted();
            }
        }).start();
        if (PreferencesUtils.hasShownStartPage()) {
            long delta = System.currentTimeMillis() - time;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterHome();
                }
            }, 2000 - delta);
        } else {
            MTAManager.generalClickEvent(LauncherActivity.this, "show_start_page");
            enterText.setVisibility(View.VISIBLE);
            termText.setVisibility(View.VISIBLE);
            gmsSettingLayout.setVisibility(View.VISIBLE);
            CheckBox gmsCb= (CheckBox) gmsSettingLayout.findViewById(R.id.gms_cb);
            DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    boolean orig = PreferencesUtils.isGMSEnable();
                    switch (i) {
                        case UpDownDialog.NEGATIVE_BUTTON:
                            break;
                        case UpDownDialog.POSITIVE_BUTTON:
                            PreferencesUtils.setGMSEnable(!orig);
                            VirtualCore.get().restart();
                            boolean newStatus = PreferencesUtils.isGMSEnable();
                            MTAManager.setGMS(LauncherActivity.this, newStatus, "startPage");
                            if (newStatus) {
                                Toast.makeText(LauncherActivity.this, getString(R.string.settings_gms_enable_toast), Toast.LENGTH_SHORT);
                            } else {
                                Toast.makeText(LauncherActivity.this, getString(R.string.settings_gms_disable_toast), Toast.LENGTH_SHORT);
                            }
                            break;
                    }
                    gmsCb.setChecked(PreferencesUtils.isGMSEnable());
                }
            };
            gmsCb.setChecked(PreferencesUtils.isGMSEnable());
            gmsCb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MTAManager.generalClickEvent(LauncherActivity.this, "start_page_gms_switch");
                    if(PreferencesUtils.isGMSEnable()) {
                        UpDownDialog.show(LauncherActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_gms_disable_notice),
                                getString(R.string.no_thanks), getString(R.string.yes), -1,
                                R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                gmsCb.setChecked(PreferencesUtils.isGMSEnable());
                            }
                        });
                    } else {
                        UpDownDialog.show(LauncherActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_gms_enable_notice),
                                getString(R.string.no_thanks), getString(R.string.yes), -1,
                                R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                gmsCb.setChecked(PreferencesUtils.isGMSEnable());
                            }
                        });
                    }
                }
            });
            SpannableString spanText=new SpannableString("Term of Service and Privacy Policy");
            spanText.setSpan(new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);  //设置文件颜色
                    ds.setColor(getResources().getColor(R.color.text_gray_dark));
                    ds.setUnderlineText(true);      //设置下划线
                }

                @Override
                public void onClick(View view) {
                    MLogs.d("Spannable onclick");
                    Intent intent = new Intent(LauncherActivity.this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_terms_of_service));
                    intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/term_of_service.html");
                    startActivity(intent);
                }
            }, 0, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new ClickableSpan() {

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);  //设置文件颜色
                    ds.setColor(getResources().getColor(R.color.text_gray_dark));
                    ds.setUnderlineText(true);      //设置下划线
                }

                @Override
                public void onClick(View view) {
                    MLogs.d("Spannable onclick");
                    Intent intent = new Intent(LauncherActivity.this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_privacy_policy));
                    intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/privacy_policy.html");
                    startActivity(intent);
                }
            }, 20, spanText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            termText.setText(spanText);
            termText.setHighlightColor(Color.TRANSPARENT);
            termText.setMovementMethod(LinkMovementMethod.getInstance());
            enterText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PreferencesUtils.setStartPageStatus(true);
                    MTAManager.generalClickEvent(LauncherActivity.this, "click_enter_home");
                    enterHome();
                    if(!PreferencesUtils.isShortCutCreated() && !created) {
                        PreferencesUtils.setShortCutCreated();
                        createShortCut();
                        created = true;
                    }
                }
            });
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

    private void enterHome(){
        startActivity(new Intent(LauncherActivity.this, HomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, -1);
        finish();
    }
}
