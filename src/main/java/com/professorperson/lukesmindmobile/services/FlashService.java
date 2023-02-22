package com.professorperson.lukesmindmobile.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlashService extends Service {

    private boolean flash = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flashToggle();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void flashToggle() {
        flash = !flash;

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Camera camera = Camera.open();
        Camera.Parameters params =  camera.getParameters();
        if (flash) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            params.setFlashMode(null);
        }
        camera.setParameters(params);
        camera.startPreview();
    }
}
