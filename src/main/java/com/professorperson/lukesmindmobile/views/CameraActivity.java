package com.professorperson.lukesmindmobile.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.services.FlashService;

import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private CameraManager manager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private TextureView cameraPreview;
    private TextureView.SurfaceTextureListener textureListener;
    private CaptureRequest.Builder requestBuilder;
    private CameraCaptureSession cameraSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreview = findViewById(R.id.cameraPreview);
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {}


        textureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                requestPermission(Manifest.permission.CAMERA, 1);
                try {
                    startCamera();
                } catch (CameraAccessException e) {}
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();

        startService(new Intent(this, FlashService.class));
        cameraPreview.setSurfaceTextureListener(textureListener);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        cameraDevice.close();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] { permission }, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                try {
                    startCamera();
                } catch (CameraAccessException e) {}
            }
        }
    }

    private void startCamera() throws CameraAccessException {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        manager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;

                try {
                    Toast.makeText(getApplicationContext(), "Camera activated", Toast.LENGTH_LONG).show();
                    startCameraPreview();
                } catch (Exception e) {}

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                cameraDevice.close();
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }, null);


    }

    private void startCameraPreview() throws CameraAccessException {
        SurfaceTexture texture = cameraPreview.getSurfaceTexture();
        cameraPreview.getWidth();
        texture.setDefaultBufferSize(2000, 2000);
        Surface surface = new Surface(texture);

        requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        requestBuilder.addTarget(surface);
        cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                if (cameraDevice == null) {
                    return;
                }
                cameraSession = session;
                updatePreview();
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }
        requestBuilder.set(CaptureRequest.CONTROL_CAPTURE_INTENT, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        try {
            cameraSession.setRepeatingRequest(requestBuilder.build(),null, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
