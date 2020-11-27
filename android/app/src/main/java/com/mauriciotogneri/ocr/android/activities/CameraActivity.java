package com.mauriciotogneri.ocr.android.activities;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.mauriciotogneri.ocr.android.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OnImageSavedCallback;
import androidx.camera.core.ImageCapture.OutputFileResults;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.ImageProxy.PlaneProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class CameraActivity extends AppCompatActivity implements Analyzer
{
    private ImageCapture imageCapture;
    protected File downloads;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private PreviewView previewView;
    private boolean previewEnabled = true;

    private static final int REQUEST_PERMISSIONS = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    private PreviewView previewView()
    {
        if (previewView == null)
        {
            previewView = findViewById(R.id.preview);
        }

        return previewView;
    }

    protected void enablePreview()
    {
        previewEnabled = true;
        previewView().setVisibility(View.VISIBLE);
        checkCamera();
    }

    protected void disablePreview()
    {
        previewEnabled = false;
        previewView().setVisibility(View.GONE);
        checkCamera();
    }

    protected boolean togglePreview()
    {
        if (previewEnabled)
        {
            disablePreview();
        }
        else
        {
            enablePreview();
        }

        return previewEnabled;
    }

    protected void checkCamera()
    {
        if (permissionsGranted())
        {
            startCamera();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
        }
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try
            {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                preview.setSurfaceProvider(previewView().getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView().getDisplay().getRotation())
                        .build();

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                imageAnalyzer.setAnalyzer(executor, this);

                cameraProvider.unbindAll();

                if (previewEnabled)
                {
                    cameraProvider.bindToLifecycle(this,
                                                   CameraSelector.DEFAULT_BACK_CAMERA,
                                                   preview,
                                                   imageCapture,
                                                   imageAnalyzer);
                }
                else
                {
                    cameraProvider.bindToLifecycle(this,
                                                   CameraSelector.DEFAULT_BACK_CAMERA,
                                                   imageCapture,
                                                   imageAnalyzer);
                }
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
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null)
        {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            analyze(imageProxy, image);
        }
        else
        {
            imageProxy.close();
        }
    }

    public abstract void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image);

    protected com.mauriciotogneri.ocr.Image bitmapToImage(@NonNull Bitmap bitmap)
    {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        return new com.mauriciotogneri.ocr.Image(bitmap.getWidth(), bitmap.getHeight(), pixels);
    }

    protected Bitmap imageToBitmap(@NonNull com.mauriciotogneri.ocr.Image image)
    {
        Bitmap bitmap = Bitmap.createBitmap(image.width, image.height, Config.ARGB_8888);
        bitmap.setPixels(image.pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        return bitmap;
    }

    protected Bitmap bitmapOriginal(@NonNull ImageProxy imageProxy)
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

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    protected Bitmap bitmap(@NonNull ImageProxy imageProxy)
    {
        PlaneProxy[] planes = imageProxy.getPlanes();
        ImageProxy.PlaneProxy yPlane = planes[0];
        ImageProxy.PlaneProxy uPlane = planes[1];
        ImageProxy.PlaneProxy vPlane = planes[2];

        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();
        yBuffer.rewind();
        uBuffer.rewind();
        vBuffer.rewind();

        int ySize = yBuffer.remaining();

        int position = 0;
        // TODO(b/115743986): Pull these bytes from a pool instead of allocating for every image.
        byte[] nv21 = new byte[ySize + (imageProxy.getWidth() * imageProxy.getHeight() / 2)];

        // Add the full y buffer to the array. If rowStride > 1, some padding may be skipped.
        for (int row = 0; row < imageProxy.getHeight(); row++)
        {
            yBuffer.get(nv21, position, imageProxy.getWidth());
            position += imageProxy.getWidth();
            yBuffer.position(Math.min(ySize, yBuffer.position() - imageProxy.getWidth() + yPlane.getRowStride()));
        }

        int chromaHeight = imageProxy.getHeight() / 2;
        int chromaWidth = imageProxy.getWidth() / 2;
        int vRowStride = vPlane.getRowStride();
        int uRowStride = uPlane.getRowStride();
        int vPixelStride = vPlane.getPixelStride();
        int uPixelStride = uPlane.getPixelStride();

        // Interleave the u and v frames, filling up the rest of the buffer. Use two line buffers to
        // perform faster bulk gets from the byte buffers.
        byte[] vLineBuffer = new byte[vRowStride];
        byte[] uLineBuffer = new byte[uRowStride];
        for (int row = 0; row < chromaHeight; row++)
        {
            vBuffer.get(vLineBuffer, 0, Math.min(vRowStride, vBuffer.remaining()));
            uBuffer.get(uLineBuffer, 0, Math.min(uRowStride, uBuffer.remaining()));
            int vLineBufferPosition = 0;
            int uLineBufferPosition = 0;
            for (int col = 0; col < chromaWidth; col++)
            {
                nv21[position++] = vLineBuffer[vLineBufferPosition];
                nv21[position++] = uLineBuffer[uLineBufferPosition];
                vLineBufferPosition += vPixelStride;
                uLineBufferPosition += uPixelStride;
            }
        }

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    protected void takePhoto(String fileName)
    {
        File file = new File(downloads, fileName);
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new OnImageSavedCallback()
        {
            @Override
            public void onImageSaved(@NonNull OutputFileResults outputFileResults)
            {
                /*try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), outputFileResults.getSavedUri());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }*/
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception)
            {
            }
        });
    }

    protected void takePhoto(OnBitmapDone onDone)
    {
        try
        {
            File outputFile = new File(getCacheDir(), String.format("%s.jpg", System.currentTimeMillis()));
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new OnImageSavedCallback()
            {
                @Override
                public void onImageSaved(@NonNull OutputFileResults outputFileResults)
                {
                    try
                    {
                        Bitmap bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 10, bitmap.getHeight() / 10, false);

                        onDone.onDone(scaled);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception)
                {
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface OnBitmapDone
    {
        void onDone(Bitmap bitmap);
    }

    protected void saveFile(Bitmap bitmap, File file)
    {
        try
        {
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.JPEG, 100, outStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void vibrate()
    {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        else
        {
            vibrator.vibrate(500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS)
        {
            checkCamera();
        }
    }

    private boolean permissionsGranted()
    {
        return (ContextCompat.checkSelfPermission(this, permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    protected void playSound()
    {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.meow);
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        executor.shutdown();
    }
}