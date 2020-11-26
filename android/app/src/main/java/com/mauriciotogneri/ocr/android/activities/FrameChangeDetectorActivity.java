package com.mauriciotogneri.ocr.android.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.TextView;

import com.google.mlkit.vision.common.InputImage;
import com.mauriciotogneri.ocr.android.R;
import com.mauriciotogneri.ocr.android.graphic.Pixel;

import org.joda.time.DateTime;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class FrameChangeDetectorActivity extends CameraActivity implements Analyzer
{
    private TextView textView;
    private File downloads;
    private float threshold;
    private float lastValue = 0;

    public static final String PARAMETER_THRESHOLD = "threshold";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_change_detector_activity);

        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        textView = findViewById(R.id.result);

        threshold = getIntent().getFloatExtra(PARAMETER_THRESHOLD, 0.5f);

        checkCamera();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage inputImage)
    {
        Bitmap bitmap = bitmap(imageProxy);
        float value = bitmapValue(bitmap);

        if (Math.abs(value - lastValue) > threshold)
        {
            lastValue = value;
            save(bitmap);
            vibrate();
            runOnUiThread(() -> textView.setText(String.valueOf(value)));
        }

        imageProxy.close();
    }

    private float bitmapValue(@NonNull Bitmap bitmap)
    {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int sum = 0;

        for (int currentPixel : pixels)
        {
            Pixel pixel = new Pixel(currentPixel);
            sum += pixel.average();
        }

        return sum / (float) (bitmap.getWidth() * bitmap.getHeight());
    }

    private void save(Bitmap bitmap)
    {
        DateTime dateTime = new DateTime(System.currentTimeMillis());
        File file = new File(downloads, String.format("%s.jpg", dateTime.toString("dd-MM-yyyy HH-mm-ss")));
        saveFile(bitmap, file);
    }

    private void vibrate()
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
}