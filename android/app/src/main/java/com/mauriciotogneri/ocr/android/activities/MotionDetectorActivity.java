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
import com.mauriciotogneri.ocr.Pixel;
import com.mauriciotogneri.ocr.android.R;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class MotionDetectorActivity extends CameraActivity implements Analyzer
{
    private TextView textView;
    private ImageView diffView;
    private int threshold;
    private int limit;
    private int status = 0;
    private Image lastImage = null;

    public static final String PARAMETER_THRESHOLD = "threshold";
    public static final String PARAMETER_LIMIT = "limit";

    private static final int STATUS_PREVIEW = 0;
    private static final int STATUS_DIFF = 1;
    private static final int STATUS_NONE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_detector_activity);

        textView = findViewById(R.id.result);
        diffView = findViewById(R.id.diff);

        Button buttonPreview = findViewById(R.id.button_preview);
        buttonPreview.setOnClickListener(v -> {
            status = (status + 1) % 3;

            if (status == STATUS_PREVIEW)
            {
                enablePreview();
                diffView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                buttonPreview.setText("PREVIEW");
            }
            else if (status == STATUS_DIFF)
            {
                disablePreview();
                diffView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                buttonPreview.setText("DIFF");
            }
            else if (status == STATUS_NONE)
            {
                disablePreview();
                diffView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                buttonPreview.setText("NONE");
            }
        });

        threshold = getIntent().getIntExtra(PARAMETER_THRESHOLD, 0);
        limit = getIntent().getIntExtra(PARAMETER_LIMIT, 0);

        checkCamera();
    }

    @Override
    @SuppressLint("UnsafeExperimentalUsageError")
    public void analyze(@NonNull ImageProxy imageProxy)
    {
        if ((status == STATUS_DIFF) || (status == STATUS_NONE))
        {
            Bitmap bitmap = bitmap(imageProxy);
            processBitmap(bitmap, imageProxy);
        }
        else
        {
            imageProxy.close();
        }
    }

    private void processBitmap(Bitmap bitmap, ImageProxy imageProxy)
    {
        Image image = bitmapToImage(bitmap);

        if (lastImage != null)
        {
            int[] diffPixels = new int[image.width * image.height];
            int whitePixels = 0;

            for (int x = 0; x < image.width; ++x)
            {
                for (int y = 0; y < image.height; ++y)
                {
                    Pixel pixelA = image.pixel(x, y);
                    Pixel pixelB = lastImage.pixel(x, y);
                    double diff = pixelA.diff(pixelB);
                    int offset = x + y * image.width;

                    if (diff >= (double) threshold)
                    {
                        diffPixels[offset] = Pixel.WHITE.value;
                        whitePixels++;
                    }
                    else
                    {
                        diffPixels[offset] = Pixel.BLACK.value;
                    }
                }
            }

            Image diffImage = new Image(image.width, image.height, diffPixels);
            Bitmap diffBitmap = imageToBitmap(diffImage);

            if (whitePixels > limit)
            {
                saveImage(diffBitmap, whitePixels);
            }

            final int label = whitePixels;

            runOnUiThread(() -> {
                diffView.setImageBitmap(diffBitmap);
                textView.setText(String.valueOf(label));
            });
        }

        lastImage = image;
        imageProxy.close();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage inputImage)
    {
    }

    private void saveImage(Bitmap bitmap, int limit)
    {
        String fileName = String.format("%s %s.jpg", DateTime.now().toString("dd-MM-yyyy HH-mm-ss-SSS"), limit);
        saveFile(bitmap, fileName);
    }
}