package com.mauriciotogneri.ocr.android.activities;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mlkit.vision.common.InputImage;
import com.mauriciotogneri.ocr.Image;
import com.mauriciotogneri.ocr.android.R;
import com.mauriciotogneri.ocr.android.graphic.Pixel;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class MotionDetectorActivity extends CameraActivity implements Analyzer
{
    private TextView textView;
    private ImageView diffView;
    private float threshold;
    private float lastValue = 0;
    private long lastFrame = 0;
    private Image lastImage = null;

    public static final String PARAMETER_THRESHOLD = "threshold";
    private static final int ANALYSIS_FREQUENCY = 3 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_detector_activity);

        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        textView = findViewById(R.id.result);
        diffView = findViewById(R.id.diff);

        threshold = getIntent().getFloatExtra(PARAMETER_THRESHOLD, 0.5f);

        checkCamera();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage inputImage)
    {
        long now = System.currentTimeMillis();
        long diff = now - lastFrame;
        Bitmap bitmap = bitmap(imageProxy);

        /*if (diff > ANALYSIS_FREQUENCY)
        {
            lastFrame = now;

            float value = bitmapValue(bitmap);

            if (Math.abs(value - lastValue) > threshold)
            {
                lastValue = value;
                saveImage();
                vibrate();
                runOnUiThread(() -> textView.append(value + "\n"));
            }
        }*/

        Image image = bitmapToImage(bitmap);

        if (lastImage != null)
        {
            Image diffImage = image.diff(lastImage, 0.1f);
            runOnUiThread(() -> diffView.setImageBitmap(imageToBitmap(diffImage)));
        }

        lastImage = image;

        imageProxy.close();
    }

    private Image bitmapToImage(Bitmap bitmap)
    {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        return new Image(bitmap.getWidth(), bitmap.getHeight(), pixels);
    }

    private Bitmap imageToBitmap(Image image)
    {
        Bitmap bitmap = Bitmap.createBitmap(image.width, image.height, Config.ARGB_8888);
        bitmap.setPixels(image.pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        return bitmap;
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

    private void saveImage()
    {
        String fileName = String.format("%s.jpg", DateTime.now().toString("dd-MM-yyyy HH-mm-ss-SSS"));
        takePhoto(fileName);
    }
}