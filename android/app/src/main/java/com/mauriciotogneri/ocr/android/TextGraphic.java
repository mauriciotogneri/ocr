package com.mauriciotogneri.ocr.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;
import com.mauriciotogneri.ocr.android.GraphicOverlay.Graphic;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TextGraphic extends Graphic
{
    private final Paint rectPaint;
    private final Paint textPaint;
    private final List<TextBlock> blocks;

    public TextGraphic(GraphicOverlay overlay, List<TextBlock> blocks)
    {
        super(overlay);

        this.blocks = blocks;

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
        for (TextBlock block : blocks)
        {
            for (Line line : block.getLines())
            {
                RectF rect = fromBoundingBox(line.getBoundingBox());
                canvas.drawRect(rect, rectPaint);
            }

            RectF rect = fromBoundingBox(block.getBoundingBox());

            float textSize = (rect.bottom - rect.top) / block.getLines().size() * 0.9f;
            textPaint.setTextSize(textSize);

            canvas.drawText(block.getText(), rect.left, rect.top + (textSize * 0.9f), textPaint);
        }
    }

    private RectF fromBoundingBox(Rect boundingBox)
    {
        RectF rect = new RectF(boundingBox);
        float x0 = translateX(rect.left);
        float x1 = translateX(rect.right);
        rect.left = min(x0, x1);
        rect.right = max(x0, x1);
        rect.top = translateY(rect.top);
        rect.bottom = translateY(rect.bottom);

        return rect;
    }
}