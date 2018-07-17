package com.example.bluelight.torch;

import android.content.Context;
import android.os.Build;

public class TorchManager {
    private static TorchManager instance;

    static {
        TorchManager.instance = null;
    }

    private FlashController controller;
    private boolean isTorchOn;

    private TorchManager() {
        super();
        this.controller = null;
        this.isTorchOn = false;
    }

    public static TorchManager getInstance() {
        if (TorchManager.instance == null) {
            TorchManager.instance = new TorchManager();
        }

        return TorchManager.instance;
    }

    public void init(Context context) {
        if (this.controller == null) {
            this.controller = Build.VERSION.SDK_INT >= 23 ? new CameraV2(context) : new CameraV1();
        }
    }

    public boolean isTorchOn() {
        return this.isTorchOn;
    }

    public void setTorch(boolean enable) {
        if (this.controller == null) {
            return;
        }

        if (enable) {
            this.controller.on();
        } else {
            this.controller.off();
        }

        this.isTorchOn = enable;
    }
}

