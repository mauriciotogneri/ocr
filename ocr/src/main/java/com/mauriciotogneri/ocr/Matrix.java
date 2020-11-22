package com.mauriciotogneri.ocr;

public class Matrix
{
    public final int width;
    public final int height;
    public final boolean[] data;

    public Matrix(int width, int height, boolean[] data)
    {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public boolean cell(int x, int y)
    {
        return data[x + (y * width)];
    }
}