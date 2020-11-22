package com.mauriciotogneri.ocr.android;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class MainActivity extends CameraActivity implements Analyzer
{
    private GraphicOverlay overlay;
    private Translator englishSpanishTranslator;
    private final TextRecognizer textRecognizer = TextRecognition.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        overlay = findViewById(R.id.overlay);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.FRENCH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build();

        englishSpanishTranslator = Translation.getClient(options);

        downloadModel(TranslateLanguage.SPANISH, task1 -> {
            downloadModel(TranslateLanguage.FRENCH, task2 -> {
                checkCamera();
            });
        });
    }

    private void downloadModel(String language, OnCompleteListener<Void> listener)
    {
        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        TranslateRemoteModel spanishModel = new TranslateRemoteModel.Builder(language).build();
        modelManager.download(spanishModel, conditions)
                .addOnSuccessListener(v -> listener.onComplete(null))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy, @NonNull InputImage image)
    {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> analyzeText(image, text))
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void analyzeText(InputImage image, Text text)
    {
        overlay.setImageSourceInfo(image.getWidth(), image.getHeight(), false);
        overlay.clear();

        for (Text.TextBlock block : text.getTextBlocks())
        {
            for (Text.Line line : block.getLines())
            {
            }
        }

        overlay.add(new TextGraphic(overlay, text.getTextBlocks()));

        /*englishSpanishTranslator
                .translate(resultText)
                .addOnSuccessListener(translatedText -> textView.setText(translatedText))
                .addOnFailureListener(Throwable::printStackTrace);*/
    }
}