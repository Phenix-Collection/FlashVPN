package com.polestar.booster;

import android.view.WindowManager;

public interface WindowView {

    WindowManager.LayoutParams createLayoutParams();

    void closeImmediate();
}
