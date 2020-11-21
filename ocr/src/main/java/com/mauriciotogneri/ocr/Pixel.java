package com.mauriciotogneri.ocr;

public class Pixel
{
    public final int alpha;
    public final int red;
    public final int green;
    public final int blue;

    public Pixel(int value)
    {
        this.alpha = value >> 24 & 255;
        this.red = value >> 16 & 255;
        this.green = value >> 8 & 255;
        this.blue = value & 255;
    }
}