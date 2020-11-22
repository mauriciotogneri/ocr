package com.mauriciotogneri.ocr.android.activities;

import android.os.Bundle;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;
import com.mauriciotogneri.ocr.android.R;
import com.mauriciotogneri.ocr.android.graphic.GraphicOverlay;
import com.mauriciotogneri.ocr.android.graphic.LabelGraphic;

import java.util.List;

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

        LocalModel localModel = new LocalModel.Builder()
                .setAssetFilePath("")
                .build();

        CustomImageLabelerOptions options = new CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
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
        overlay.clear();
        overlay.add(new LabelGraphic(overlay, objects));
        overlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), false);
    }
}