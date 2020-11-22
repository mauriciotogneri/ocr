package com.mauriciotogneri.ocr.android.activities;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.mauriciotogneri.ocr.android.R;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ImageProxy.PlaneProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class CameraActivity extends AppCompatActivity implements Analyzer
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int REQUEST_CAMERA_PERMISSION = 10;

    protected void checkCamera()
    {
        if (cameraPermissionGranted())
        {
            startCamera();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    protected void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try
            {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                PreviewView previewView = findViewById(R.id.preview);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                imageAnalyzer.setAnalyzer(executor, this);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,
                                               CameraSelector.DEFAULT_BACK_CAMERA,
                                               preview,
                                               imageAnalyzer);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    @SuppressLint("UnsafeExperimentalUsageError")
    public void analyze(@NonNull ImageProxy imageProxy)
    {
        imageProxy.getImageInfo().getRotationDegrees();
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null)
        {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            analyze(imageProxy, image);
        }
    }

    public abstract void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image);

    protected Bitmap bitmap(@NonNull ImageProxy imageProxy)
    {
        PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        //bitmap.recycle();

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /*private void saveFile(ImageProxy imageProxy, Bitmap bitmap)
    {
        try
        {
            File file = new File(downloads, String.format("%s.jpg", imageProxy.getImageInfo().getTimestamp()));
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, outStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if (cameraPermissionGranted())
            {
                startCamera();
            }
        }
    }

    private boolean cameraPermissionGranted()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        executor.shutdown();
    }
}