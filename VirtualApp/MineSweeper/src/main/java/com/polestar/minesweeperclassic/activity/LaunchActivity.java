package com.polestar.minesweeperclassic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by doriscoco on 2017/4/3.
 */

public class LaunchActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent start = new Intent();
        start.setClass(this, GameActivity.class);
        startActivity(start);
        finish();
    }
}
