package com.mauriciotogneri.ocr.android.activities;

import android.os.Bundle;
import android.os.Environment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.mauriciotogneri.ocr.android.R;
import com.mauriciotogneri.ocr.android.graphic.GraphicOverlay;
import com.mauriciotogneri.ocr.android.graphic.LabelGraphic;

import org.joda.time.DateTime;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class CatDetectorActivity extends CameraActivity implements Analyzer
{
    private ImageLabeler imageLabeler;
    private GraphicOverlay overlay;
    private File downloads;

    public static final String PARAMETER_CONFIDENCE = "confidence";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cat_detector_activity);

        overlay = findViewById(R.id.overlay);

        downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        float confidence = getIntent().getFloatExtra(PARAMETER_CONFIDENCE, 0.5f);

        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(confidence)
                .build();

        imageLabeler = ImageLabeling.getClient(options);

        checkCamera();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image)
    {
        imageLabeler.process(image)
                .addOnSuccessListener(objects -> objectsDetected(imageProxy, objects))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void objectsDetected(@NonNull ImageProxy imageProxy, @NonNull List<ImageLabel> objects)
    {
        List<ImageLabel> filtered = objects.stream()
                .filter(this::isAnimal)
                .collect(Collectors.toList());

        if (!filtered.isEmpty())
        {
            playSound();

            DateTime dateTime = new DateTime();
            String timestamp = dateTime.toString("dd-MM-yyyy HH:mm:ss:SSS");
            String keys = keys(filtered);
            File file = new File(downloads, String.format("%s %s.jpg", timestamp, keys));
            takePhoto(file);
        }

        overlay.clear();
        overlay.add(new LabelGraphic(overlay, filtered));
        overlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), false);
    }

    private boolean isAnimal(@NonNull ImageLabel imageLabel)
    {
        String label = imageLabel.getText().toLowerCase().trim();

        return label.equals("cat") || label.equals("dog");
    }

    @NonNull
    private String keys(@NonNull List<ImageLabel> imageLabels)
    {
        StringBuilder builder = new StringBuilder();

        for (ImageLabel imageLabel : imageLabels)
        {
            if (builder.length() != 0)
            {
                builder.append(" ");
            }

            builder.append(imageLabel.getText().toLowerCase());
            builder.append("=");
            builder.append((int) (imageLabel.getConfidence() * 100));
        }

        return builder.toString();
    }
}