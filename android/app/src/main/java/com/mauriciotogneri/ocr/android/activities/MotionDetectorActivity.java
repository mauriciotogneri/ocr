package com.mauriciotogneri.ocr.android.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mlkit.vision.common.InputImage;
import com.mauriciotogneri.ocr.Image;
import com.mauriciotogneri.ocr.android.R;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class MotionDetectorActivity extends CameraActivity implements Analyzer
{
    private TextView textView;
    private ImageView diffView;
    private float threshold;
    private long lastFrame = 0;
    private Image lastImage = null;

    public static final String PARAMETER_THRESHOLD = "threshold";
    private static final int ANALYSIS_FREQUENCY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_detector_activity);

        textView = findViewById(R.id.result);
        diffView = findViewById(R.id.diff);

        Button buttonPreview = findViewById(R.id.button_preview);
        buttonPreview.setOnClickListener(v -> {
            if (togglePreview())
            {
                buttonPreview.setText("OFF");
                diffView.setVisibility(View.GONE);
            }
            else
            {
                buttonPreview.setText("ON");
                diffView.setVisibility(View.VISIBLE);
            }
        });

        threshold = getIntent().getFloatExtra(PARAMETER_THRESHOLD, 0f);

        checkCamera();
        diffView.setVisibility(View.GONE);
    }

    @Override
    @SuppressLint("UnsafeExperimentalUsageError")
    public void analyze(@NonNull ImageProxy imageProxy)
    {
        long now = System.currentTimeMillis();
        long diff = now - lastFrame;

        if (diff > ANALYSIS_FREQUENCY)
        {
            lastFrame = now;

            Bitmap bitmap = bitmap(imageProxy);
            Image image = bitmapToImage(bitmap);

            if (lastImage != null)
            {
                //long start1 = System.currentTimeMillis();
                Image diffImage = image.diff(lastImage, threshold);
                //long end1 = System.currentTimeMillis();
                //Log.d("IMAGE_DEBUG", (end1 - start1) + "ms");
                runOnUiThread(() -> diffView.setImageBitmap(imageToBitmap(diffImage)));
            }

            lastImage = image;
        }

        imageProxy.close();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage inputImage)
    {
    }

    private void saveImage()
    {
        String fileName = String.format("%s.jpg", DateTime.now().toString("dd-MM-yyyy HH-mm-ss-SSS"));
        takePhoto(fileName);
    }
}