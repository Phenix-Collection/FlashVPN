package com.google.android.gms.booster;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

public class BoosterShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String from = getIntent().getStringExtra(BoosterSdk.EXTRA_SHORTCUT_CLICK_FROM );
        Booster.startCleanShortcutClick(this.getApplicationContext(), TextUtils.isEmpty(from)? "launcher":from);

        finish();
    }
}
