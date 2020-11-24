package com.mauriciotogneri.ocr.android.activities;

import android.content.Intent;
import android.os.Bundle;

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
            Intent intent = new Intent(this, CatDetectorActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.button_translator).setOnClickListener(v -> {
            Intent intent = new Intent(this, TextTranslatorActivity.class);
            startActivity(intent);
            finish();
        });
    }
}