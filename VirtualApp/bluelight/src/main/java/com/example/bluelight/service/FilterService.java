package com.example.bluelight.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.bluelight.R;
import com.example.bluelight.utils.AlarmUtils;
import com.example.bluelight.utils.ColorUtils;
import com.example.bluelight.utils.DeviceUtils;
import com.example.bluelight.utils.NotificationUtils;
import com.example.bluelight.utils.PreferenceUtils;
import com.example.bluelight.view.FilterView;

import java.util.Calendar;

public class FilterService extends Service {
    public static final String ACTION_ALARM_EXPIRE = "ACTION_ALARM_EXPIRE";
    public static final String ACTION_PAUSE_COMMAND = "ACTION_PAUSE";
    public static final String ACTION_START_AUTO_ENABLE_COMMAND = "ACTION_START_AUTO_ENABLE";
    public static final String ACTION_START_COMMAND = "ACTION_START";
    public static final String ACTION_STOP_AUTO_ENABLE_COMMAND = "ACTION_STOP_AUTO_ENABLE";
    public static final String ACTION_STOP_COMMAND = "ACTION_STOP";
    public static final String ACTION_TOGGLE_FILTER = "ACTION_TOGGLE_FILTER";
    private FilterView mView;

    public FilterService() {
        super();
    }

    public static void pauseFilter(Context arg1) {
        FilterService.sendAction(arg1, ACTION_PAUSE_COMMAND);
    }

    public static void sendAction(Context context, String action) {
        Intent intent = new Intent(context, FilterService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    public static void startAutoEnable(Context context) {
        FilterService.sendAction(context, ACTION_START_AUTO_ENABLE_COMMAND);
    }

    public static void startFilter(Context context) {
        FilterService.sendAction(context, ACTION_START_COMMAND);
    }

    public static void stopAutoEnable(Context context) {
        FilterService.sendAction(context, ACTION_STOP_AUTO_ENABLE_COMMAND);
    }

    public static void stopFilter(Context context) {
        FilterService.sendAction(context, ACTION_STOP_COMMAND);
    }

    private void addViews() {
        if (this.mView == null) {
            this.mView = new FilterView(((Context) this));
            int type = 2006;
            if (Build.VERSION.SDK_INT >= 26) {
                type = 2038;
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, type, 16779032, -3);
            Point displaySize = DeviceUtils.getDisplaySize(((Context) this));
            params.height = displaySize.y + DeviceUtils.getNavigationBarHeight(((Context) this));
            params.width = displaySize.x;
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) {
                return;
            }

            wm.addView(this.mView, params);
        }
    }

    private void alarmExpire() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int time = PreferenceUtils.formatTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        int startTime = PreferenceUtils.getAutoEnableStartTime(((Context) this));
        int stopTime = PreferenceUtils.getAutoEnableStopTime(((Context) this));
        boolean enable = false;
        if (startTime < stopTime) {
            if (time >= startTime && time < stopTime) {
                enable = true;
            } else {

                enable = false;
            }
        } else {
            if (time >= stopTime) {
                if (time >= startTime) {
                    enable = true;

                } else {
                }
            }

        }

        PreferenceUtils.setFilterStatus(((Context) this), enable);
        if (enable) {
            this.setFilterColor();
        } else {
            this.removeViews();
        }

        AlarmUtils.setupAlarm(((Context) this));
    }

    @Override
    public IBinder onBind(Intent arg3) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        this.removeViews();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START_COMMAND:
                    this.setFilterColor();
                    return START_STICKY;

                case ACTION_STOP_COMMAND:
                    this.removeViews();
                    return START_STICKY;

                case ACTION_PAUSE_COMMAND:
                    this.setTransparent();
                    return START_STICKY;

                case ACTION_START_AUTO_ENABLE_COMMAND:
                    AlarmUtils.setupAlarm(((Context) this));
                    this.alarmExpire();
                    return START_STICKY;

                case ACTION_STOP_AUTO_ENABLE_COMMAND:
                    AlarmUtils.cancelAlarm(((Context) this));
                    return START_STICKY;

                case ACTION_ALARM_EXPIRE:
                    this.alarmExpire();
                    return START_STICKY;

                case ACTION_TOGGLE_FILTER:
                    this.toggleFilter();
                    return START_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void removeViews() {
        if (this.mView != null) {
            Object wm = this.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                ((WindowManager) wm).removeView(this.mView);
                this.mView = null;
            }
        }
    }

    private void setFilterColor() {
        this.addViews();
        int filterColor = ColorUtils.getForegroundColor(PreferenceUtils.getColorTemperature(((Context) this)),
                PreferenceUtils.getIntensity(((Context) this)));
        int dimColor = ColorUtils.getBackgroundColor(PreferenceUtils.getScreenDim(((Context) this)));
        this.mView.setFilterColor(filterColor);
        this.mView.setDimColor(dimColor);
        this.mView.invalidate();
    }

    private void setTransparent() {
        if (this.mView != null) {
            this.mView.setFilterColor(ContextCompat.getColor(((Context) this), R.color.transparent));
            this.mView.setDimColor(ContextCompat.getColor(((Context) this), R.color.transparent));
            this.mView.invalidate();
        }
    }

    private void toggleFilter() {
        boolean status = PreferenceUtils.isFilterEnable(((Context) this));
        PreferenceUtils.setFilterStatus(((Context) this), !status);
        if (status) {
            this.removeViews();
        } else {
            this.setFilterColor();
        }

        NotificationUtils.createNotification(((Context) this));
    }
}

