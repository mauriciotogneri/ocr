package com.mauriciotogneri.ocr.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class CustomView extends View
{
    private int width = 0;
    private int height = 0;
    private final List<Rect> areas = new ArrayList<>();
    private Paint paint;

    public CustomView(Context context)
    {
        super(context);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public synchronized void areas(InputImage image, List<Rect> areas)
    {
        if ((image.getRotationDegrees() == 0) || (image.getRotationDegrees() == 180))
        {
            this.width = image.getWidth();
            this.height = image.getHeight();
        }
        else
        {
            this.width = image.getHeight();
            this.height = image.getWidth();
        }

        this.areas.clear();
        this.areas.addAll(areas);
        invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (paint == null)
        {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.FILL);
        }

        int canvasWidth = getWidth();
        int canvasHeight = getHeight();

        for (Rect rect : areas)
        {
            int left = (int) (((float) rect.left / (float) width) * canvasWidth);
            int top = (int) (((float) rect.top / (float) height) * canvasHeight);
            int right = (int) (((float) rect.right / (float) width) * canvasWidth);
            int bottom = (int) (((float) rect.bottom / (float) height) * canvasHeight);
            Rect rectAdjusted = new Rect(left, top, right, bottom);

            canvas.drawRect(rectAdjusted, paint);
        }
    }
}
