package com.professorperson.lukesmindmobile.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.printservice.CustomPrinterIconCallback;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.professorperson.lukesmindmobile.R;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;


public class CameraActivity extends AppCompatActivity implements Runnable {

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private TextView debug;

    private CameraManager manager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private TextureView cameraPreview;
    private TextureView.SurfaceTextureListener textureListener;
    private CaptureRequest.Builder requestBuilder;
    private CameraCaptureSession cameraSession;

    private StompClient stompClient;

    private Thread thread;
    private boolean running = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        debug = findViewById(R.id.debug);
        cameraPreview = findViewById(R.id.cameraPreview);

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {}

        textureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
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
//                if (delta >= 1) {
//                    delta = 0;
//                    sendFrame();
//                }
            }
        };

        thread = new Thread(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        stop();
        startActivity(new Intent(this, MainActivity.class));
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://lukemind.herokuapp.com/frame/websocket");
        stompClient.connect();

        cameraPreview.setSurfaceTextureListener(textureListener);

        running = true;
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {

        }

        cameraDevice.close();
    }

    @Override
    public void run() {
        loop();
    }

    private double delta;

    private void loop() {
        int fps = 1;
        double time_per_frame = 1000000000 / fps;
        delta = 0; // per frame (when to switch frames)
        long prev_time = System.nanoTime();

        while (running) {
            long time = System.nanoTime();
            delta += (time - prev_time) / time_per_frame;
            prev_time = time;

            if (delta >= 1) {
                delta = 0;
                sendFrame();
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
            cameraSession.setRepeatingRequest(requestBuilder.build(), null, new Handler());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private void sendFrame() {
            Bitmap image = cameraPreview.getBitmap();

            if (image == null) {
                return;
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 15, os);
            String base64 = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);

            stompClient.send("/app/send_frame", base64).subscribe();
    }


}
