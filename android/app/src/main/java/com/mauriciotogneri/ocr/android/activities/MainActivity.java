package com.mauriciotogneri.ocr.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.mauriciotogneri.ocr.android.R;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findViewById(R.id.button_catDetector).setOnClickListener(v -> {
            EditText confidenceField = findViewById(R.id.confidence);
            float confidence = Float.parseFloat(confidenceField.getText().toString());

            Intent intent = new Intent(this, CatDetectorActivity.class);
            intent.putExtra(CatDetectorActivity.PARAMETER_CONFIDENCE, confidence);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button_translator).setOnClickListener(v -> {
            Intent intent = new Intent(this, TextTranslatorActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button_frameChange).setOnClickListener(v -> {
            EditText thresholdField = findViewById(R.id.threshold);
            float threshold = Float.parseFloat(thresholdField.getText().toString());

            Intent intent = new Intent(this, FrameChangeDetectorActivity.class);
            intent.putExtra(FrameChangeDetectorActivity.PARAMETER_THRESHOLD, threshold);
            startActivity(intent);
            finish();
        });
    }
}