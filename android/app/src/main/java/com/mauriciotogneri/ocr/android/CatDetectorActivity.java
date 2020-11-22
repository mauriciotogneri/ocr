package com.mauriciotogneri.ocr.android;

import android.os.Bundle;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class CatDetectorActivity extends CameraActivity implements Analyzer
{
    private ObjectDetector objectDetector;
    private GraphicOverlay overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cat_detector_activity);

        overlay = findViewById(R.id.overlay);

        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);

        checkCamera();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image)
    {
        objectDetector.process(image)
                .addOnSuccessListener(objects -> objectsDetected(imageProxy, objects))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void objectsDetected(@NonNull ImageProxy imageProxy, @NonNull List<DetectedObject> objects)
    {
        overlay.clear();

        for (DetectedObject object : objects)
        {
            overlay.add(new ObjectGraphic(overlay, object));
        }

        overlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), false);
    }
}