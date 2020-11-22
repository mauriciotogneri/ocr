package com.mauriciotogneri.ocr.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.mauriciotogneri.ocr.android.GraphicOverlay.Graphic;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TextGraphic extends Graphic
{
    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text text;

    public TextGraphic(GraphicOverlay overlay, Text text)
    {
        super(overlay);

        this.text = text;

        this.rectPaint = new Paint();
        this.rectPaint.setColor(Color.WHITE);
        this.rectPaint.setStyle(Style.FILL);

        this.textPaint = new Paint();
        this.textPaint.setColor(Color.BLACK);

        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas)
    {
        for (TextBlock textBlock : text.getTextBlocks())
        {
            for (Line line : textBlock.getLines())
            {
                RectF rect = new RectF(line.getBoundingBox());
                float x0 = translateX(rect.left);
                float x1 = translateX(rect.right);
                rect.left = min(x0, x1);
                rect.right = max(x0, x1);
                rect.top = translateY(rect.top);
                rect.bottom = translateY(rect.bottom);
                canvas.drawRect(rect, rectPaint);

                float textSize = (rect.bottom - rect.top) * 0.9f;
                textPaint.setTextSize(textSize);

                canvas.drawText(line.getText(), rect.left, rect.top + (textSize * 0.9f), textPaint);
            }
        }
    }
}