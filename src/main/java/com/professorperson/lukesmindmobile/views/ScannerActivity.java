package com.professorperson.lukesmindmobile.views;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.professorperson.lukesmindmobile.R;
import com.professorperson.lukesmindmobile.models.Message;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.util.concurrent.ExecutionException;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class ScannerActivity extends AppCompatActivity implements Runnable {

    private PreviewView cameraPreviewView;
    private Button recordBtn;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private StompClient stompClient;
    private Thread thread;
    private boolean running = false;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    public ScannerActivity() {
        thread = new Thread(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        checkCameraPermissions(this);
        openCamera();

        cameraPreviewView = findViewById(R.id.cameraView);


        recordBtn = findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(v -> {
            if (!running) {
                thread.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, "ws://lukemind.herokuapp.com/frame/websocket");
        stompClient.connect();
    }

    @Override
    public void onBackPressed() {
        stop();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void openCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor((this)));
    }

    @SuppressWarnings("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        //preview with camera previewView for lifecycle
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).setFlashMode(ImageCapture.FLASH_MODE_ON).build();
        VideoCapture videoCapture = new VideoCapture.Builder().setVideoFrameRate(30).build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
    }

    private void capturePhoto() {
        File photoFile = new File("/images");
        if (!photoFile.exists())
            photoFile.mkdir();

        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(photoFile).build(), ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(getApplicationContext(), "Image Saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void run() {
        scan();
    }

    private void scan() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
        });


        checkCameraPermissions(this);
        running = true;

        int fps = 1;
        double time_per_frame = 1000000000 / fps;
        double delta = 0; // per frame (when to switch frames)
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

    private void sendFrame() {
       mainHandler.post(() -> {
           if (stompClient.isConnected()) {
               Bitmap image = cameraPreviewView.getBitmap();
               image.setWidth(128);
               image.setHeight(128);
               Toast.makeText(getApplicationContext(), image.getWidth() + " x " + image.getHeight(), Toast.LENGTH_SHORT).show();
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               image.compress(Bitmap.CompressFormat.JPEG, 1, bos);
               String base64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);

               Message message = new Message();
               message.setText(base64);
               stompClient.send("/app/send_message", new Gson().toJson(message)).doOnError(throwable -> {}).subscribe();
           }
       });
    }

    private void stop() {
        try {
            running = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void checkCameraPermissions(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            ActivityCompat.requestPermissions((Activity) context,
                    new String[] { Manifest.permission.CAMERA },
                    100);
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            ActivityCompat.requestPermissions((Activity) context,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    101);
        }
    }
}