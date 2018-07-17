package com.example.bluelight.torch;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

class CameraV2 implements FlashController {

    private final CameraManager cameraManager;
    private String cameraId;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    CameraV2(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {

            if (cameraManager != null) {
                cameraId = cameraManager.getCameraIdList()[0];
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void off() {
        try {
            if (cameraManager != null) {
                cameraManager.setTorchMode(cameraId, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void on() {
        try {
            if (cameraManager != null) {
                cameraManager.setTorchMode(cameraId, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

