package com.mauriciotogneri.ocr.android;

import android.os.Bundle;

import com.google.mlkit.vision.common.InputImage;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class CatDetectorActivity extends CameraActivity implements Analyzer
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cat_detector_activity);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image)
    {
    }
}