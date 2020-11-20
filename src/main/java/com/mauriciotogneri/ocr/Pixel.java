package com.mauriciotogneri.ocr;

public class Pixel
{
    private final int value;

    public Pixel(int value)
    {
        this.value = value;
    }

    public int alpha()
    {
        return value >> 24 & 255;
    }

    public int red()
    {
        return value >> 16 & 255;
    }

    public int green()
    {
        return value >> 8 & 255;
    }

    public int blue()
    {
        return value & 255;
    }
}