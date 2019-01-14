package com.polestar.minesweeperclassic.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.polestar.ad.adapters.FuseAdLoader;

/**
 * Created by doriscoco on 2017/4/3.
 */

public class LaunchActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (GameActivity.needAppStartAd()) {
            FuseAdLoader.get(GameActivity.SLOT_ENTER_INTERSTITIAL, this).preloadAd(this);
        }
        Intent start = new Intent();
        start.setClass(this, GameActivity.class);
        startActivity(start);
        finish();
    }
}
