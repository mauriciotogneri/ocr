package com.mauriciotogneri.ocr.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import com.mauriciotogneri.ocr.android.R;

import androidx.appcompat.app.AppCompatActivity;

// TODO: Push notifications
public class MainActivity extends AppCompatActivity
{
    private static final String FIELD_CONFIDENCE = "confidence";
    private static final String FIELD_THRESHOLD = "threshold";
    private static final String FIELD_LIMIT = "limit";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        EditText confidenceField = findViewById(R.id.confidence);
        confidenceField.setText(String.valueOf(loadConfidence()));

        EditText thresholdField = findViewById(R.id.threshold);
        thresholdField.setText(String.valueOf(loadThreshold()));

        EditText limitField = findViewById(R.id.limit);
        limitField.setText(String.valueOf(loadLimit()));

        findViewById(R.id.button_catDetector).setOnClickListener(v -> {
            Intent intent = new Intent(this, CatDetectorActivity.class);
            intent.putExtra(CatDetectorActivity.PARAMETER_CONFIDENCE, confidence());
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button_translator).setOnClickListener(v -> {
            Intent intent = new Intent(this, TextTranslatorActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button_motionDetector).setOnClickListener(v -> {
            Intent intent = new Intent(this, MotionDetectorActivity.class);
            intent.putExtra(MotionDetectorActivity.PARAMETER_THRESHOLD, threshold());
            intent.putExtra(MotionDetectorActivity.PARAMETER_LIMIT, limit());
            startActivity(intent);
            finish();
        });
    }

    private SharedPreferences sharedPreferences()
    {
        return getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    private float loadConfidence()
    {
        SharedPreferences sharedPreferences = sharedPreferences();

        return sharedPreferences.getFloat(FIELD_CONFIDENCE, 0.9f);
    }

    private int loadThreshold()
    {
        SharedPreferences sharedPreferences = sharedPreferences();

        return sharedPreferences.getInt(FIELD_THRESHOLD, 100);
    }

    private int loadLimit()
    {
        SharedPreferences sharedPreferences = sharedPreferences();

        return sharedPreferences.getInt(FIELD_LIMIT, 5000);
    }

    private void saveConfidence(float confidence)
    {
        SharedPreferences sharedPreferences = sharedPreferences();
        sharedPreferences.edit().putFloat(FIELD_CONFIDENCE, confidence).apply();
    }

    private void saveThreshold(int threshold)
    {
        SharedPreferences sharedPreferences = sharedPreferences();
        sharedPreferences.edit().putInt(FIELD_THRESHOLD, threshold).apply();
    }

    private void saveLimit(int limit)
    {
        SharedPreferences sharedPreferences = sharedPreferences();
        sharedPreferences.edit().putInt(FIELD_LIMIT, limit).apply();
    }

    private float confidence()
    {
        EditText confidenceField = findViewById(R.id.confidence);
        float confidence = Float.parseFloat(confidenceField.getText().toString());

        saveConfidence(confidence);

        return confidence;
    }

    private int threshold()
    {
        EditText thresholdField = findViewById(R.id.threshold);
        int threshold = Integer.parseInt(thresholdField.getText().toString());

        saveThreshold(threshold);

        return threshold;
    }

    private int limit()
    {
        EditText limitField = findViewById(R.id.limit);
        int limit = Integer.parseInt(limitField.getText().toString());

        saveLimit(limit);

        return limit;
    }
}