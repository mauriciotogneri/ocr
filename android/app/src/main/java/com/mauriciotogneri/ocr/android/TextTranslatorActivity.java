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
import com.mauriciotogneri.ocr.android.TextGraphic.TranslatedBlock;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis.Analyzer;
import androidx.camera.core.ImageProxy;

public class TextTranslatorActivity extends CameraActivity implements Analyzer
{
    private GraphicOverlay overlay;
    private Translator frenchSpanishTranslator;
    private final TextRecognizer textRecognizer = TextRecognition.getClient();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_translator_activity);

        overlay = findViewById(R.id.overlay);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.FRENCH)
                .setTargetLanguage(TranslateLanguage.SPANISH)
                .build();

        frenchSpanishTranslator = Translation.getClient(options);

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
        TextTranslator translator = new TextTranslator(overlay, frenchSpanishTranslator, image, text);
        translator.translate();
    }

    public static class TextTranslator
    {
        private final GraphicOverlay overlay;
        private final Translator translator;
        private final InputImage image;
        private final Text text;
        private final TranslatedBlock[] translatedBlocks;
        private int translatedCount = 0;

        public TextTranslator(GraphicOverlay overlay, Translator translator, InputImage image, Text text)
        {
            this.overlay = overlay;
            this.translator = translator;
            this.image = image;
            this.text = text;
            this.translatedBlocks = new TranslatedBlock[text.getTextBlocks().size()];
        }

        public void translate()
        {
            int limit = text.getTextBlocks().size();

            for (int i = 0; i < limit; i++)
            {
                final int index = i;
                Text.TextBlock block = text.getTextBlocks().get(i);

                translatedBlocks[i] = new TranslatedBlock(block.getLines());

                translator.translate(block.getText())
                        .addOnSuccessListener(textTranslated -> {
                            translatedBlocks[index].translatedText(textTranslated.trim());
                            translatedCount++;

                            if (translatedCount == translatedBlocks.length)
                            {
                                onDone();
                            }
                        })
                        .addOnFailureListener(Throwable::printStackTrace);
            }
        }

        public void onDone()
        {
            overlay.setImageSourceInfo(image.getWidth(), image.getHeight(), false);
            overlay.clear();

            overlay.add(new TextGraphic(overlay, translatedBlocks));
        }
    }
}