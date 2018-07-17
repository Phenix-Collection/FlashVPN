package com.example.bluelight.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.bluelight.R;
import com.example.bluelight.torch.TorchManager;
import com.example.bluelight.utils.NotificationUtils;
import com.example.bluelight.utils.PreferenceUtils;

public class TorchActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_SOS_STATE = "SOS_STATE";
    private static final String KEY_TORCH_STATE = "TORCH_STATE";
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    private int[] SOS_INTERVAL;
    private ImageButton mCloseButton;
    private Handler mHandler;
    private boolean mIsSosOn;
    private boolean mIsTorchOn;
    private ImageButton mSOSButton;
    private ImageView mTorchLight;
    private ImageButton mTorchSwitch;
    private PulseRunnable mSosRunnable;

    public TorchActivity() {
        super();
        this.mHandler = new Handler();
        this.mIsTorchOn = true;
        this.mIsSosOn = false;
        this.SOS_INTERVAL = new int[]{200, 200, 200, 200, 200, 600, 200, 600, 200, 600, 200, 600, 200, 200, 200, 200, 200, 1400};
        this.mSosRunnable = new PulseRunnable(this.SOS_INTERVAL);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(((Context) this), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void hideSystemUI() {
        View v0 = this.getWindow().getDecorView();
        int v1 = 1798;
        if (Build.VERSION.SDK_INT >= 19) {
            v1 |= 2048;
        }

        v0.setSystemUiVisibility(v1);
    }

    private void initStates() {
        TorchManager.getInstance().init(((Context) this));
        this.setSosStatus(this.mIsSosOn);
        this.setTorchStatus(this.mIsTorchOn);
        this.setPreferenceState(this.mIsTorchOn | this.mIsSosOn);
    }

    private void initViews() {
        this.mTorchLight = this.findViewById(R.id.torch_light);
        this.mTorchSwitch = this.findViewById(R.id.torch_switch);
        this.mCloseButton = this.findViewById(R.id.torch_close);
        this.mSOSButton = this.findViewById(R.id.torch_sos);
        this.mTorchSwitch.setOnClickListener(((View.OnClickListener) this));
        this.mCloseButton.setOnClickListener(((View.OnClickListener) this));
        this.mSOSButton.setOnClickListener(((View.OnClickListener) this));
    }

    @Override
    public void onClick(View arg2) {
        switch (arg2.getId()) {
            case R.id.torch_close: {
                this.setSosStatus(false);
                this.setTorchStatus(false);
                this.setPreferenceState(this.mIsTorchOn | this.mIsSosOn);
                this.finish();
                break;
            }
            case R.id.torch_sos: {
                this.toggleSosStatus();
                break;
            }
            case R.id.torch_switch: {
                this.toggleTorchStatus();
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
        if (savedInstanceState != null) {
            this.mIsTorchOn = savedInstanceState.getBoolean(KEY_TORCH_STATE);
            this.mIsSosOn = savedInstanceState.getBoolean(KEY_SOS_STATE);
        }

        this.setContentView(R.layout.activity_torch);
        this.hideSystemUI();
        this.initViews();
        if (Build.VERSION.SDK_INT < 23 || (this.checkCameraPermission())) {
            this.initStates();
        } else {
            this.requestCameraPermission();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 102 && grantResults[0] == 0) {
            this.initStates();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_TORCH_STATE, this.mIsTorchOn);
        outState.putBoolean(KEY_SOS_STATE, this.mIsSosOn);
    }

    public void requestCameraPermission() {
        ActivityCompat.requestPermissions(((Activity) this), new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }

    private void setPreferenceState(boolean state) {
        if (state != PreferenceUtils.isTorchOn(((Context) this))) {
            PreferenceUtils.setTorchState(((Context) this), state);
            if (PreferenceUtils.isNotificationEnable(((Context) this))) {
                NotificationUtils.createNotification(((Context) this));
            }
        }
    }

    private void setSosStatus(boolean status) {
        if (status) {
            this.turnOnSos();
            this.mTorchSwitch.setImageResource(R.drawable.torch_switch_on);
            this.mSOSButton.setImageResource(R.drawable.torch_sos_on);
        } else {
            this.turnOffSos();
            this.mSOSButton.setImageResource(R.drawable.torch_sos_off);
        }
    }

    private void setTorchStatus(boolean status) {
        if (status) {
            this.turnOnTorch();
            this.mTorchSwitch.setImageResource(R.drawable.torch_switch_on);
        } else {
            this.turnOffTorch();
            this.mTorchSwitch.setImageResource(R.drawable.torch_switch_off);
        }
    }

    private void toggleSosStatus() {
        if (this.mIsSosOn) {
            this.mIsSosOn = false;
            this.mIsTorchOn = true;
            this.setSosStatus(false);
            this.setTorchStatus(true);
        } else {
            this.mIsSosOn = true;
            this.mIsTorchOn = true;
            this.setSosStatus(true);
        }

        this.setPreferenceState(this.mIsTorchOn | this.mIsSosOn);
    }

    private void toggleTorchStatus() {
        if (this.mIsTorchOn) {
            this.mIsTorchOn = false;
            this.mIsSosOn = false;
            this.setTorchStatus(false);
            this.setSosStatus(false);
        } else {
            this.mIsTorchOn = true;
            this.setTorchStatus(true);
        }

        this.setPreferenceState(this.mIsTorchOn | this.mIsSosOn);
    }

    private void turnOffSos() {
        this.turnOffTorch();
        this.mHandler.removeCallbacks(this.mSosRunnable);
    }

    private void turnOffTorch() {
        this.mTorchLight.setVisibility(4);
        TorchManager.getInstance().setTorch(false);
    }

    private void turnOnSos() {
        this.mSosRunnable.reset();
        this.turnOffTorch();
        this.mHandler.post(this.mSosRunnable);
    }

    private void turnOnTorch() {
        this.mTorchLight.setVisibility(0);
        TorchManager.getInstance().setTorch(true);
    }

    class PulseRunnable implements Runnable {
        private final int[] interval;
        private int index;

        PulseRunnable(int[] interval) {
            super();
            this.interval = interval;
            this.index = 0;
        }

        public void reset() {
            this.index = 0;
        }

        public void run() {
            if (TorchManager.getInstance().isTorchOn()) {
                TorchActivity.this.turnOffTorch();
            } else {
                TorchActivity.this.turnOnTorch();
            }

            TorchActivity.this.mHandler.postDelayed(((Runnable) this), ((long) this.interval[this.index]));
            if (this.index == this.interval.length - 1) {
                this.index = 0;
            } else {
                ++this.index;
            }
        }
    }
}

