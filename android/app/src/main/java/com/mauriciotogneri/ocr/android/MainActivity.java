package com.mauriciotogneri.ocr.android;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

// https://developers.google.com/ml-kit/language/translation/android
public class MainActivity extends AppCompatActivity implements Analyzer
{
    private TextView textView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private File downloads;

    private static final int REQUEST_CAMERA_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        textView = findViewById(R.id.text);
        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (cameraPermissionGranted())
        {
            startCamera();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void startCamera()
    {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try
            {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                PreviewView previewView = findViewById(R.id.preview);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight()))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        //.setTargetRotation(rotation == 270 ? Surface.ROTATION_270 : Surface.ROTATION_180)
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
        analyze1(imageProxy);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void analyze1(@NonNull ImageProxy imageProxy)
    {
        imageProxy.getImageInfo().getRotationDegrees();
        Image mediaImage = imageProxy.getImage();

        if (mediaImage != null)
        {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            //image.getBitmapInternal();
            Log.d("DEBUG_IMAGE", imageProxy.getImageInfo().getRotationDegrees() + "");
            TextRecognizer recognizer = TextRecognition.getClient();
            recognizer.process(image)
                    .addOnSuccessListener(this::analyzeText)
                    .addOnFailureListener(Throwable::printStackTrace)
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private void analyzeText(Text text)
    {
        String resultText = text.getText();

        for (Text.TextBlock block : text.getTextBlocks())
        {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();

            for (Text.Line line : block.getLines())
            {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();

                for (Text.Element element : line.getElements())
                {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }

                resultText += lineText + "\n";
            }
        }

        textView.setText(resultText);
    }

    private void analyze2(@NonNull ImageProxy imageProxy)
    {
        /*long start = System.currentTimeMillis();
        Log.d("DEBUG_TIME", imageProxy.toString() + " => START");

        long start1 = System.currentTimeMillis();
        Bitmap bitmap = bitmap(imageProxy);
        Log.d("DEBUG_TIME", "GET BITMAP: " + (System.currentTimeMillis() - start1) + "ms");

        //==========================================================================================

        long start2 = System.currentTimeMillis();
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Image cameraImage = new Image(bitmap.getWidth(), bitmap.getHeight(), pixels);
        Log.d("DEBUG_TIME", "GET IMAGE: " + (System.currentTimeMillis() - start2) + "ms");

        //==========================================================================================

        long start3 = System.currentTimeMillis();
        Image binarizedImage = cameraImage.binarize();
        Log.d("DEBUG_TIME", "BINARIZING IMAGE: " + (System.currentTimeMillis() - start3) + "ms");

        //==========================================================================================

        long start4 = System.currentTimeMillis();
        int[] colors = new int[binarizedImage.width * binarizedImage.height];

        for (int x = 0; x < binarizedImage.width; x++)
        {
            for (int y = 0; y < binarizedImage.height; y++)
            {
                Pixel pixel = binarizedImage.pixel(x, y);
                colors[x + (y * binarizedImage.width)] = pixel.value;
            }
        }

        Bitmap binarizedBitmap = Bitmap.createBitmap(colors, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Log.d("DEBUG_TIME", "GETTING BINARIZED BITMAP: " + (System.currentTimeMillis() - start4) + "ms");

        //==========================================================================================

        runOnUiThread(() -> binarized.setImageBitmap(binarizedBitmap));

        //List<Symbol> symbols = cameraImage.symbols();

        //for (Symbol symbol : symbols)
        //{
        //    Image symbolImage = symbol.image();
        //    System.out.println(symbolImage);
        //}

        imageProxy.close();

        Log.d("DEBUG_TIME", imageProxy.toString() + " => END => " + (System.currentTimeMillis() - start) + "ms");*/
    }

    private Bitmap bitmap(ImageProxy imageProxy)
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
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //bitmap.recycle();
        //rotatedBitmap.recycle();

        return rotatedBitmap;
    }

    private void saveFile(ImageProxy imageProxy, Bitmap bitmap)
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
    }

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