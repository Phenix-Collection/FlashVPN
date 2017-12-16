package com.google.android.gms.booster;

import android.app.Activity;
import android.os.Bundle;

public class BoosterShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Booster.startCleanShortcutClick(this.getApplicationContext(), "launcher");

        finish();
    }
}
