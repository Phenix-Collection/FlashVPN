package com.example.bluelight.ui;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluelight.BuildConfig;
import com.example.bluelight.R;
import com.example.bluelight.receiver.MyBroadcastReceiver;
import com.example.bluelight.service.FilterService;
import com.example.bluelight.utils.NotificationUtils;
import com.example.bluelight.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final int COLOR_TEMPERATURE_1_INDEX = 0;
    public static final int COLOR_TEMPERATURE_2_INDEX = 1;
    public static final int COLOR_TEMPERATURE_3_INDEX = 2;
    public static final int COLOR_TEMPERATURE_4_INDEX = 3;
    public static final int COLOR_TEMPERATURE_5_INDEX = 4;
    public static final int COLOR_TEMPERATURE_INDEX_MAX = 4;
    public static final int MAX_INTENSITY = 80;
    public static final int MAX_SCREEN_DIM = 75;
    public static final int REQUEST_CODE_OVERLAY_PERMISSION = 101;
    private ImageButton mAppWall;
    private ImageButton mCTSelect1;
    private ImageButton mCTSelect2;
    private ImageButton mCTSelect3;
    private ImageButton mCTSelect4;
    private ImageButton mCTSelect5;
    private ImageButton[] mCTSelectButtons;
    private SeekBar mDimSeekBar;
    private DrawerLayout mDrawerLayout;
    private ImageButton mFilterSwitch;
    private TextView mIntensity;
    private SeekBar mIntensitySeekBar;
    private boolean mIsAdLoading;
    private boolean mIsResumed;
    private CardView mNativeAdCardView;
    private SwitchCompat mNavFilterSwitch;
    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("filter")) {
                boolean status = PreferenceUtils.isFilterEnable(MainActivity.this);
                MainActivity.this.updateSwitchView(status);
                MainActivity.this.mNavFilterSwitch.setChecked(status);
            }
        }
    };
    private SwitchCompat mNavNotification;
    private View mNavigationLayout;
    private SwitchCompat mNotificationSwitch;
    private TextView mPauseText;
    private TextView mScreenDim;
    private ImageButton mShareButton;
    private SwitchCompat mTimerSwitch;
    private TextView mTimerText;

    private void changeIntensity(int arg2) {
        PreferenceUtils.setIntensity(((Context) this), arg2);
        this.updateIntensityValue(arg2);
        this.setFilterStatus(true);
    }

    private void changeScreenDim(int arg2) {
        PreferenceUtils.setScreenDim(((Context) this), arg2);
        this.updateScreenDim(arg2);
        this.setFilterStatus(true);
    }

    @TargetApi(value = 23)
    public boolean checkDrawOverlayPermission() {
        return Settings.canDrawOverlays(((Context) this));
    }

    private String getAutoEnableTimeString(int start, int stop) {
        return String.format(this.getResources().getString(R.string.auto_time_fmt), this.getTimeString(start), this.getTimeString(stop));
    }

    private String getPercentageString(int arg3) {
        return arg3 + "%";
    }

    private String getTimeString(int time) {
        SimpleDateFormat v0 = new SimpleDateFormat("HH:mm");
        Date v1 = new Date();
        v1.setHours(PreferenceUtils.getHour(time));
        v1.setMinutes(PreferenceUtils.getMinutes(time));
        return v0.format(v1);
    }

    private void initAppWallStatus() {
        mAppWall.setVisibility(View.VISIBLE);
    }

    private void initNavigationView(View view) {
        this.mNavFilterSwitch = view.findViewById(R.id.nav_switch);
        this.mNavFilterSwitch.setOnClickListener(((View.OnClickListener) this));
        this.mNavNotification = view.findViewById(R.id.nav_notification);
        this.mNavNotification.setOnClickListener(((View.OnClickListener) this));
        view.findViewById(R.id.nav_help).setOnClickListener(((View.OnClickListener) this));
        view.findViewById(R.id.nav_share).setOnClickListener(((View.OnClickListener) this));
        view.findViewById(R.id.nav_rate).setOnClickListener(((View.OnClickListener) this));
        view.findViewById(R.id.nav_feedback).setOnClickListener(((View.OnClickListener) this));
        this.mNavFilterSwitch.setChecked(PreferenceUtils.isFilterEnable(((Context) this)));
        this.mNavNotification.setChecked(PreferenceUtils.isNotificationEnable(((Context) this)));
        TextView version = view.findViewById(R.id.nav_version);
        version.setText(String.format(this.getResources().getString(R.string.nav_menu_version_fmt), "1.2.2"));
    }

    private void initReceiver() {
        this.getPackageManager().setComponentEnabledSetting(new ComponentName(((Context) this),
                MyBroadcastReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void initState() {
        if (PreferenceUtils.isFilterEnable(((Context) this))) {
            this.setFilterStatus(true);
        }

        if (PreferenceUtils.isAutoEnableOn(((Context) this))) {
            FilterService.startAutoEnable(((Context) this));
        } else {
            FilterService.stopAutoEnable(((Context) this));
        }

        if (PreferenceUtils.isNotificationEnable(((Context) this))) {
            NotificationUtils.createNotification(((Context) this));
        }
    }

    private void initViews() {
        this.mDrawerLayout = this.findViewById(R.id.drawer_layout);
        this.mNavigationLayout = this.findViewById(R.id.nav_layout);
        this.mAppWall = this.findViewById(R.id.app_wall);
        this.mShareButton = this.findViewById(R.id.share);
        this.mFilterSwitch = this.findViewById(R.id.filter_switch);
        this.mCTSelect1 = this.findViewById(R.id.color_1);
        this.mCTSelect2 = this.findViewById(R.id.color_2);
        this.mCTSelect3 = this.findViewById(R.id.color_3);
        this.mCTSelect4 = this.findViewById(R.id.color_4);
        this.mCTSelect5 = this.findViewById(R.id.color_5);
        this.mCTSelectButtons = new ImageButton[]{this.mCTSelect1, this.mCTSelect2, this.mCTSelect3, this.mCTSelect4, this.mCTSelect5};
        this.mIntensity = this.findViewById(R.id.intensity_value);
        this.mIntensitySeekBar = this.findViewById(R.id.intensity_seekbar);
        this.mTimerSwitch = this.findViewById(R.id.auto_enable_switch);
        this.mTimerText = this.findViewById(R.id.auto_enable_time);
        this.mNotificationSwitch = this.findViewById(R.id.notification_switch);
        this.mScreenDim = this.findViewById(R.id.dim_value);
        this.mDimSeekBar = this.findViewById(R.id.dim_seekbar);
        this.mPauseText = this.findViewById(R.id.pause_subtitle);
        this.mNativeAdCardView = this.findViewById(R.id.native_ad_card);
        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        this.mAppWall.setOnClickListener(((View.OnClickListener) this));
        this.mShareButton.setOnClickListener(((View.OnClickListener) this));
        this.mFilterSwitch.setOnClickListener(((View.OnClickListener) this));
        this.mCTSelect1.setOnClickListener(((View.OnClickListener) this));
        this.mCTSelect2.setOnClickListener(((View.OnClickListener) this));
        this.mCTSelect3.setOnClickListener(((View.OnClickListener) this));
        this.mCTSelect4.setOnClickListener(((View.OnClickListener) this));
        this.mCTSelect5.setOnClickListener(((View.OnClickListener) this));
        this.mTimerSwitch.setOnClickListener(((View.OnClickListener) this));
        this.mTimerText.setOnClickListener(((View.OnClickListener) this));
        this.mNotificationSwitch.setOnClickListener(((View.OnClickListener) this));
        this.mPauseText.setOnClickListener(((View.OnClickListener) this));
        this.mIntensitySeekBar.setOnSeekBarChangeListener(((SeekBar.OnSeekBarChangeListener) this));
        this.mDimSeekBar.setOnSeekBarChangeListener(((SeekBar.OnSeekBarChangeListener) this));
        this.mDimSeekBar.setMax(MAX_SCREEN_DIM);
        this.mIntensitySeekBar.setMax(MAX_INTENSITY);
        this.initNavigationView(this.mNavigationLayout);
        this.refreshView();
        this.initReceiver();
        this.initState();
        this.initAppWallStatus();
        PreferenceUtils.registerOnSharedPreferenceChangeListener(((Context) this), this.mPreferenceChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION && (this.checkDrawOverlayPermission())) {
            this.setFilterStatus(true);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_wall: {
                this.openAppWall();
                break;
            }
            case R.id.auto_enable_switch: {
                this.toggleAutoEnable();
                break;
            }
            case R.id.auto_enable_time: {
                this.selectTime();
                break;
            }
            case R.id.color_1: {
                this.selectColor(COLOR_TEMPERATURE_1_INDEX);
                break;
            }
            case R.id.color_2: {
                this.selectColor(COLOR_TEMPERATURE_2_INDEX);
                break;
            }
            case R.id.color_3: {
                this.selectColor(COLOR_TEMPERATURE_3_INDEX);
                break;
            }
            case R.id.color_4: {
                this.selectColor(COLOR_TEMPERATURE_4_INDEX);
                break;
            }
            case R.id.color_5: {
                this.selectColor(COLOR_TEMPERATURE_5_INDEX);
                break;
            }
            case R.id.filter_switch: {
                this.toggleFilter();
                break;
            }
            case R.id.nav_feedback: {
                this.openFeedback();
                this.mDrawerLayout.closeDrawers();
                break;
            }
            case R.id.nav_help: {
                this.openHelp();
                this.mDrawerLayout.closeDrawers();
                break;
            }
            case R.id.nav_notification: {
                this.toggleNotification();
                break;
            }
            case R.id.nav_rate: {
                this.openRate(((Context) this));
                this.mDrawerLayout.closeDrawers();
                break;
            }
            case R.id.nav_share: {
                this.shareApp();
                this.mDrawerLayout.closeDrawers();
                break;
            }
            case R.id.nav_switch: {
                this.toggleFilter();
                break;
            }
            case R.id.notification_switch: {
                this.toggleNotification();
                break;
            }
            case R.id.pause_subtitle: {
                this.togglePause();
                break;
            }
            case R.id.share: {
                this.shareApp();
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceUtils.unregisterOnSharedPreferenceChangeListener(((Context) this), this.mPreferenceChangeListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != android.R.id.home) {
            return super.onOptionsItemSelected(menuItem);
        }

        this.mDrawerLayout.openDrawer(8388611);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mIsResumed = false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int v0 = seekBar.getId();
            if (v0 == R.id.dim_seekbar) {
                this.changeScreenDim(progress);
            } else if (v0 != R.id.intensity_seekbar) {
            } else {
                this.changeIntensity(progress);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mIsResumed = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.mAppWall.getVisibility() == View.VISIBLE) {
            ((AnimationDrawable) this.mAppWall.getDrawable()).start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void openAppWall() {
    }

    private void openFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mypretty.boatmobile@gmail.com"));
        intent.putExtra(Intent.EXTRA_TEXT, "--------------------------------------------\nPlease keep the following information\n--------------------------------------------\nApp: " + this.getString(R.string.app_name) + "\nApp Version: " + "1.2.2" + "\nModel: " + Build.MODEL + "\nRegion: " + this.getResources().getConfiguration().locale.getCountry() + "\nLanguage: " + this.getResources().getConfiguration().locale.getLanguage() + "\nOS Type: Android\nOS Version:" + Build.VERSION.SDK_INT + "\n\n");
        try {
            this.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void openHelp() {
        HelpActivity.startHelp(((Context) this));
    }

    private void openRate(Context context) {
        Intent v0 = new Intent(Intent.ACTION_VIEW);
        v0.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        if (v0.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(v0);
        } else {
            v0.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
            if (v0.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(v0);
            } else {
                Toast.makeText(context, R.string.rate_failure, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void refreshView() {
        this.updateSwitchView(PreferenceUtils.isFilterEnable(((Context) this)));
        int v0 = PreferenceUtils.getIntensity(((Context) this));
        this.updateIntensityValue(v0);
        this.mIntensitySeekBar.setProgress(v0);
        this.updateCTSelection(PreferenceUtils.getColorTemperatureIndex(((Context) this)), -1);
        this.mTimerSwitch.setChecked(PreferenceUtils.isAutoEnableOn(((Context) this)));
        TextView v1 = this.mTimerText;
        int v2 = PreferenceUtils.isAutoEnableOn(((Context) this)) ? R.color.main_view_time_text : R.color.main_view_title_text;
        v1.setTextColor(ContextCompat.getColor(((Context) this), v2));
        this.updateAutoEnableTime(PreferenceUtils.getAutoEnableStartTime(((Context) this)), PreferenceUtils.getAutoEnableStopTime(((Context) this)));
        this.mNotificationSwitch.setChecked(PreferenceUtils.isNotificationEnable(((Context) this)));
        v0 = PreferenceUtils.getScreenDim(((Context) this));
        this.updateScreenDim(v0);
        this.mDimSeekBar.setProgress(v0);
    }

    @TargetApi(value = 23)
    public void requestDrawOverlayPermission() {
        StringBuilder v2 = new StringBuilder();
        v2.append("package:");
        v2.append(this.getPackageName());
        this.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                , Uri.parse(v2.toString())), REQUEST_CODE_OVERLAY_PERMISSION);
    }

    private void selectColor(int index) {
        if (index < 0 || index > COLOR_TEMPERATURE_INDEX_MAX)
            return;

        int[] color_temperature = getResources().getIntArray(R.array.color_temperature);
        this.setColorTemperature(index, color_temperature[index]);
    }

    private void selectTime() {
    }

    private void setColorTemperature(int index, int ct) {
        int oldIndex = PreferenceUtils.getColorTemperatureIndex(((Context) this));
        PreferenceUtils.setColorTemperature(((Context) this), ct);
        PreferenceUtils.setColorTemperatureIndex(((Context) this), index);
        this.updateCTSelection(index, oldIndex);
        this.setFilterStatus(true);
    }

    private void setFilterStatus(boolean status) {
        if ((status) && Build.VERSION.SDK_INT >= 23 && !this.checkDrawOverlayPermission()) {
            this.requestDrawOverlayPermission();
            return;
        }

        this.setFilterStatusImpl(status);
        PreferenceUtils.setFilterStatus(((Context) this), status);
        this.updateSwitchView(status);
        this.mNavFilterSwitch.setChecked(status);
        NotificationUtils.createNotification(((Context) this));
    }

    private void setFilterStatusImpl(boolean status) {
        if (status) {
            FilterService.startFilter(((Context) this));
        } else {
            FilterService.stopFilter(((Context) this));
        }
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        try {
            this.startActivity(intent);
        } catch (Exception v2) {
            v2.printStackTrace();
        }
    }

//    public void showNativeAd(TranNativeAdView arg8, TranNativeAd arg9) {
//        arg8.setAdView(arg9, arg8.getContext().getSystemService("layout_inflater").inflate(R.layout.layout_native_ad, null));
//        VideoController v1 = arg9.getVideoController();
//        if(v1 != null) {
//            v1.setVideoLifecycleCallbacks(new VideoLifecycleCallbacks() {
//                public void onVideoEnd() {
//                    super.onVideoEnd();
//                }
//            });
//        }
//
//        View v2 = arg8.findViewById(R.id.main_image);
//        if(v1 == null || !v1.hasVideoContent()) {
//            arg8.setImageView(((ImageView)v2));
//            List v3 = arg9.getImages();
//            if(v3 != null && !v3.isEmpty()) {
//                if(v3.get(0).getDrawable() != null) {
//                    ((ImageView)v2).setImageDrawable(v3.get(0).getDrawable());
//                }
//                else if(v3.get(0).getUri() != null) {
//                    Glide.with(App.getContext()).load(v3.get(0).getUri().toString()).into(((ImageView)v2));
//                }
//            }
//        }
//        else {
//            ((ImageView)v2).setVisibility(View.GONE);
//        }
//
//        View v3_1 = arg8.findViewById(R.id.icon_image);
//        arg8.setIconView(v3_1);
//        Image v4 = arg9.getIcon();
//        if(v4 != null) {
//            if(v4.getDrawable() != null) {
//                ((ImageView)v3_1).setImageDrawable(v4.getDrawable());
//            }
//            else if(v4.getUri() != null) {
//                Glide.with(App.getContext()).load(v4.getUri().toString()).into(((ImageView)v3_1));
//            }
//        }
//
//        arg8.setHeadlineView(arg8.findViewById(R.id.headline_text));
//        arg8.setBodyView(arg8.findViewById(R.id.body_text));
//        arg8.setCallToActionView(arg8.findViewById(R.id.install_button));
//        arg8.getHeadlineView().setText(arg9.getHeadline());
//        arg8.getBodyView().setText(arg9.getBody());
//        arg8.getCallToActionView().setText(arg9.getCallToAction());
//        arg8.registerView(arg9);
//    }

    private void toggleAutoEnable() {
        boolean status = !PreferenceUtils.isAutoEnableOn(((Context) this));
        PreferenceUtils.setAutoEnableStatus(((Context) this), ((boolean) status));
        TextView v1 = this.mTimerText;
        int v2 = status ? R.color.main_view_time_text : R.color.main_view_title_text;
        v1.setTextColor(ContextCompat.getColor(((Context) this), v2));
        if (status) {
            FilterService.startAutoEnable(((Context) this));
        } else {
            FilterService.stopAutoEnable(((Context) this));
        }
    }

    private void toggleFilter() {
        this.setFilterStatus(!PreferenceUtils.isFilterEnable(((Context) this)));
    }

    private void toggleNotification() {
        boolean status = !PreferenceUtils.isNotificationEnable(((Context) this));
        PreferenceUtils.setNotificationStatus(((Context) this), ((boolean) status));
        if (status) {
            NotificationUtils.createNotification(((Context) this));
        } else {
            NotificationUtils.cancelNotification(((Context) this));
        }

        this.mNavNotification.setChecked(((boolean) status));
        this.mNotificationSwitch.setChecked(((boolean) status));
    }

    private void togglePause() {
    }

    private void updateAutoEnableTime(int arg3, int arg4) {
        this.mTimerText.setText(this.getAutoEnableTimeString(arg3, arg4));
    }

    private void updateCTSelection(int newIndex, int oldIndex) {
        if (newIndex < 0 || newIndex > COLOR_TEMPERATURE_INDEX_MAX)
            return;

        int[] normals = new int[]{R.drawable.ic_color_1_normal, R.drawable.ic_color_2_normal,
                R.drawable.ic_color_3_normal, R.drawable.ic_color_4_normal, R.drawable.ic_color_5_normal};
        int[] presses = new int[]{R.drawable.ic_color_1_pressed, R.drawable.ic_color_2_pressed,
                R.drawable.ic_color_3_pressed, R.drawable.ic_color_4_pressed, R.drawable.ic_color_5_pressed};
        if (oldIndex != -1) {
            this.mCTSelectButtons[oldIndex].setImageResource(normals[oldIndex]);
        } else {
            for (int i = 0; i < this.mCTSelectButtons.length; ++i) {
                this.mCTSelectButtons[i].setImageResource(normals[i]);
            }
        }

        this.mCTSelectButtons[newIndex].setImageResource(presses[newIndex]);
    }

    private void updateIntensityValue(int intensity) {
        this.mIntensity.setText(this.getPercentageString(intensity));
    }

    private void updateScreenDim(int dim) {
        this.mScreenDim.setText(this.getPercentageString(dim));
    }

    private void updateSwitchView(boolean arg3) {
        if (arg3) {
            this.mFilterSwitch.setImageResource(R.drawable.switch_button_on);
        } else {
            this.mFilterSwitch.setImageResource(R.drawable.switch_button_off);
        }
    }
}

