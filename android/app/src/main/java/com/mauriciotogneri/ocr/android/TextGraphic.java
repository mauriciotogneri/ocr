package com.mauriciotogneri.ocr.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.mlkit.vision.text.Text.Line;
import com.mauriciotogneri.ocr.android.GraphicOverlay.Graphic;
import com.mauriciotogneri.ocr.android.MainActivity.TranslatedBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class TextGraphic extends Graphic
{
    private final Paint rectPaint;
    private final Paint textPaint;
    private final TranslatedBlock[] translatedBlocks;

    public TextGraphic(GraphicOverlay overlay, TranslatedBlock[] translatedBlocks)
    {
        super(overlay);

        this.translatedBlocks = translatedBlocks;

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
        for (TranslatedBlock block : translatedBlocks)
        {
            for (Line line : block.lines)
            {
                RectF rect = fromBoundingBox(line.getBoundingBox());

                float extra = rect.height() * 0.2f;
                RectF newRect = new RectF(rect.left - extra,
                                          rect.top - extra,
                                          rect.right + extra,
                                          rect.bottom + extra);
                canvas.drawRect(newRect, rectPaint);
            }

            List<String> remainingText = new ArrayList<>(Arrays.asList(block.translatedText.split(" ")));
            int linesCount = block.lines.size();

            for (int i = 0; i < linesCount; i++)
            {
                Line line = block.lines.get(i);

                RectF rect = fromBoundingBox(line.getBoundingBox());
                canvas.drawRect(rect, rectPaint);

                float textSize = (rect.bottom - rect.top) * 0.8f;
                textPaint.setTextSize(textSize);

                StringBuilder currentLine = new StringBuilder();

                if (i < (linesCount - 1))
                {
                    while (((currentLine.length() * (textSize / 2.2f)) < rect.width()) && !remainingText.isEmpty())
                    {
                        String firstWord = remainingText.get(0);

                        if (((currentLine.length() + firstWord.length()) * (textSize / 2.2f)) < rect.width())
                        {
                            remainingText.remove(0);
                            currentLine.append(firstWord);
                            currentLine.append(" ");
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                else
                {
                    for (String element : remainingText)
                    {
                        currentLine.append(element);
                        currentLine.append(" ");
                    }
                }

                canvas.drawText(currentLine.toString().trim(), rect.left, rect.top + textSize, textPaint);
            }

            /*RectF rect = fromBoundingBox(block.getBoundingBox());

            float textSize = (rect.bottom - rect.top) / block.getLines().size() * 0.9f;
            textPaint.setTextSize(textSize);

            canvas.drawText(block.getText(), rect.left, rect.top + (textSize * 0.9f), textPaint);*/
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