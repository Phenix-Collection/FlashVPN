package com.google.android.gms.booster.view;

import android.view.WindowManager;

public interface WindowView {

    WindowManager.LayoutParams createLayoutParams();

    void closeImmediate();
}
