package com.polestar.multiaccount.widgets.locker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.activity.AppCloneActivity;
import com.polestar.multiaccount.component.activity.LockSecureQuestionActivity;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.ResourcesUtil;
import com.polestar.multiaccount.widgets.FeedbackImageView;
import com.polestar.multiaccount.widgets.FloatWindow;
import com.polestar.multiaccount.widgets.PopupMenu;

/**
 * Created by guojia on 2017/1/3.
 */

public class AppLockWindow implements PopupMenu.OnMenuItemSelectedListener {

    private PopupMenu mPopupMenu;

    private String mAppName;
    private Handler mHandler;
    private FloatWindow mWindow;
    private View mContentView;

    private boolean mIsShowing;

    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    public AppLockWindow(String appName, Handler handler) {
        mAppName = appName;
        mHandler = handler;

        mWindow = new FloatWindow(MApp.getApp());

        mContentView = LayoutInflater.from(MApp.getApp()).inflate(R.layout.applock_window_layout, null);

        mWindow.setContentView(mContentView);
        mWindow.setOnBackPressedListener(new FloatWindow.OnBackPressedListener() {
            @Override
            public void onBackPressed() {
                CommonUtils.gotoHomeScreen();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 500);
            }
        });

        mAppLockPasswordLogic = new AppLockPasswordLogic(mContentView, new AppLockPasswordLogic.EventListener() {
            @Override
            public void onCorrectPassword() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppLockWindow window = AppLockWindowManager.getInstance().get(mAppName);
                        if (window != null && window.isShowing()) {
                            window.dismiss();
                        }
                    }
                }, 500);
            }

            @Override
            public void onIncorrectPassword() {
            }

            @Override
            public void onCancel() {
            }
        });
        mAppLockPasswordLogic.onFinishInflate();

        initToolbar();

        TextView text = (TextView) mContentView.findViewById(R.id.window_applock_name);
        String title =  mAppName;
        if (!TextUtils.isEmpty(title)){
            text.setText(String.format(ResourcesUtil.getString(R.string.applock_window_title),title));
        }

        FeedbackImageView icon = (FeedbackImageView) mContentView.findViewById(R.id.window_applock_icon);
        PackageManager pm = MApp.getApp().getPackageManager();
        Drawable drawable = null;
        try {
            drawable = pm.getApplicationIcon(mAppName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (drawable != null) {
            icon.setImageBitmap( BitmapUtils.createCustomIcon(MApp.getApp(), drawable));
        }
    }

    private void initToolbar() {
        if (mContentView == null) return;

        View menuLayout = LayoutInflater.from(MApp.getApp()).inflate(R.layout.menu_applock_toolbar, null);
        menuLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mPopupMenu = new PopupMenu((ViewGroup)menuLayout);
        mPopupMenu.setOnMenuItemSelectedListener(this);

        final float offsetX = DisplayUtils.dip2px(MApp.getApp(),3);
        final float offsetY = DisplayUtils.dip2px(MApp.getApp(),-10);
        final float menuWidth = menuLayout.getMeasuredWidth();
        final View menu = mContentView.findViewById(R.id.cmx_toolbar_applock_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupMenu.isShowing()) {
                    mPopupMenu.dismiss();
                } else {
                    mPopupMenu.show(menu, (int) (menu.getWidth() - offsetX - menuWidth), (int) offsetY);
                }
            }
        });
    }

    public void show() {
        if (!mIsShowing) {
            mAppLockPasswordLogic.onShow();
            mWindow.show();
            mIsShowing = true;
        }
    }

    public void dismiss() {
        if (mIsShowing && mPopupMenu != null && mWindow != null) {
            mAppLockPasswordLogic.onBeforeHide();
            mPopupMenu.dismiss();
            mWindow.hide();
            mIsShowing = false;
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    @Override
    public void onMenuItemSelected(View menuItem) {
        Intent intent = null;
        switch (menuItem.getId()) {
            case R.id.menu_forgot_password:
                if (PreferencesUtils.isSafeQuestionSet(MApp.getApp())) {
                    intent = new Intent(MApp.getApp(), LockSecureQuestionActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(LockSecureQuestionActivity.EXTRA_TITLE, ResourcesUtil.getString(R.string.al_change_security_question));
                    intent.putExtra(LockSecureQuestionActivity.EXTRA_RESET, false);
                }
                try {
                    if (intent != null){
                        MApp.getApp().startActivity(intent);
                    }
                }finally {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AppLockWindow window = AppLockWindowManager.getInstance().get(mAppName);
                            if (window != null && window.isShowing()) {
                                window.dismiss();
                            }
                        }
                    }, 1000);
                }
                break;
        }
    }
}