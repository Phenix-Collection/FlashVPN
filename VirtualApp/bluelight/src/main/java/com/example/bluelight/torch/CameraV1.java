package com.example.bluelight.torch;

import android.hardware.Camera;

class CameraV1 implements FlashController {
    private Camera camera;

    @Override
    public void off() {
        try {
            if (camera != null) {
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(p);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void on() {
        try {
            off();
            if (camera == null) {
                try {
                    camera = Camera.open(getCameraId());
                } catch (RuntimeException ex) {
                    System.out.println("Runtime error while opening camera!");
                }
            }
            if (camera != null) {
                Camera.Parameters params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getCameraId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return 0;
    }
}

