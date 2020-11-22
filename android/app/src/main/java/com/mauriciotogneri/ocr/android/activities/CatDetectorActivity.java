package com.mauriciotogneri.ocr.android.activities;

import android.os.Bundle;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.mauriciotogneri.ocr.android.R;
import com.mauriciotogneri.ocr.android.graphic.GraphicOverlay;
import com.mauriciotogneri.ocr.android.graphic.LabelGraphic;

import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class CatDetectorActivity extends CameraActivity implements Analyzer
{
    private ImageLabeler imageLabeler;
    private GraphicOverlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cat_detector_activity);

        overlay = findViewById(R.id.overlay);

        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f)
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
        List<ImageLabel> cats = objects.stream().filter(imageLabel -> imageLabel.getText().trim().toLowerCase().equals("cat")).collect(Collectors.toList());

        overlay.clear();
        overlay.add(new LabelGraphic(overlay, cats));
        overlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), false);
    }
}