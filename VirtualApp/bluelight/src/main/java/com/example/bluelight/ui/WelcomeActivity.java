package com.example.bluelight.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.bluelight.R;
import com.example.bluelight.utils.PreferenceUtils;

public class WelcomeActivity extends Activity {
    private static final int SPLASH_TIME = 3000;
    private Runnable enterAppRunnable = new Runnable() {
        @Override
        public void run() {
            WelcomeActivity.this.enterApp();
        }
    };
    private Handler handler;

    public WelcomeActivity() {
        super();
        this.handler = new Handler();
    }

    private void enterApp() {
        Intent v0;
        if (PreferenceUtils.isFirstUse(((Context) this))) {
            v0 = new Intent(((Context) this), HelpActivity.class);
            PreferenceUtils.setFirstUse(((Context) this), false);
        } else {
            v0 = new Intent(((Context) this), MainActivity.class);
        }

        this.startActivity(v0);
        this.finish();
    }

    private void hideSystemUI() {
        View v0 = this.getWindow().getDecorView();
        int v1 = 1798;
        if (Build.VERSION.SDK_INT >= 19) {
            v1 |= 2048;
        }

        v0.setSystemUiVisibility(v1);
    }

    protected void onCreate(Bundle arg5) {
        super.onCreate(arg5);
        this.setContentView(R.layout.activity_welcome);
        this.hideSystemUI();
        if (!PreferenceUtils.isFirstUse(((Context) this))) {
        }

        this.handler.postDelayed(this.enterAppRunnable, SPLASH_TIME);
    }
}
