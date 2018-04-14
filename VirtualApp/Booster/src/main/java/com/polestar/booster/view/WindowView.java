package com.polestar.booster.view;

import android.view.WindowManager;

public interface WindowView {

    WindowManager.LayoutParams createLayoutParams();

    void closeImmediate();
}
