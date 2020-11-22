package com.mauriciotogneri.ocr.android.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.mlkit.vision.label.ImageLabel;

import java.util.List;
import java.util.Locale;

public class LabelGraphic extends GraphicOverlay.Graphic
{
    private static final float TEXT_SIZE = 70.0f;
    private static final String LABEL_FORMAT = "%.2f%%";

    private final Paint textPaint;
    private final Paint labelPaint;
    private final GraphicOverlay overlay;

    private final List<ImageLabel> labels;

    public LabelGraphic(GraphicOverlay overlay, List<ImageLabel> labels)
    {
        super(overlay);

        this.overlay = overlay;
        this.labels = labels;

        this.textPaint = new Paint();
        this.textPaint.setColor(Color.WHITE);
        this.textPaint.setTextSize(TEXT_SIZE);

        this.labelPaint = new Paint();
        this.labelPaint.setColor(Color.BLACK);
        this.labelPaint.setStyle(Paint.Style.FILL);
        this.labelPaint.setAlpha(200);
    }

    @Override
    public synchronized void draw(Canvas canvas)
    {
        float maxWidth = 0;
        float totalHeight = labels.size() * 2 * TEXT_SIZE;

        for (ImageLabel label : labels)
        {
            float line1Width = textPaint.measureText(label.getText());
            float line2Width =
                    textPaint.measureText(
                            String.format(
                                    Locale.US, LABEL_FORMAT, label.getConfidence() * 100));
            maxWidth = Math.max(maxWidth, Math.max(line1Width, line2Width));

            Log.d("IMAGE_DEBUG", label.getText());
        }
        float x = Math.max(0, overlay.getWidth() / 2.0f - maxWidth / 2.0f);
        float y = Math.max(200, overlay.getHeight() / 2.0f - totalHeight / 2.0f);

        if (!labels.isEmpty())
        {
            float padding = 20;
            canvas.drawRect(
                    x - padding, y - padding, x + maxWidth + padding, y + totalHeight + padding, labelPaint);
        }

        for (ImageLabel label : labels)
        {
            if (y + TEXT_SIZE * 2 > overlay.getHeight())
            {
                break;
            }
            canvas.drawText(label.getText(), x, y + TEXT_SIZE, textPaint);
            y += TEXT_SIZE;
            canvas.drawText(
                    String.format(Locale.US, LABEL_FORMAT, label.getConfidence() * 100),
                    x,
                    y + TEXT_SIZE,
                    textPaint);
            y += TEXT_SIZE;
        }
    }
}