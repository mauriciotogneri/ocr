package com.mauriciotogneri.ocr;

public class Pixel
{
    public final int value;
    public final int alpha;
    public final int red;
    public final int green;
    public final int blue;

    public Pixel(int value)
    {
        this.value = value;
        this.alpha = value >> 24 & 0xff;
        this.red = value >> 16 & 0xff;
        this.green = value >> 8 & 0xff;
        this.blue = value & 0xff;
    }

    public int average()
    {
        return (red + green + blue) / 3;
    }

    public Pixel grayScale()
    {
        int average = average();
        int blackAndWhite = (alpha << 24) | (average << 16) | (average << 8) | average;

        return new Pixel(blackAndWhite);
    }

    public Pixel binarize()
    {
        int average = average();
        int value = (average > 127) ? 255 : 0;
        int binarized = (alpha << 24) | (value << 16) | (value << 8) | value;

        return new Pixel(binarized);
    }

    public interface Transform
    {
        Pixel transform(Pixel pixel);
    }
}