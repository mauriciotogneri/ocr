package com.mauriciotogneri.ocr;

public class Symbol
{
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final Matrix matrix;

    public Symbol(int x, int y, Matrix matrix)
    {
        this.x = x;
        this.y = y;
        this.width = matrix.width;
        this.height = matrix.height;
        this.matrix = matrix;
    }

    public Image image()
    {
        int[][] pixels = new int[width][height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                boolean cell = matrix.cell(x, y);
                Pixel pixel;

                if (cell)
                {
                    pixel = new Pixel(255, 0, 0, 0);
                }
                else
                {
                    pixel = new Pixel(255, 255, 255, 255);
                }

                pixels[x][y] = pixel.value;
            }
        }

        return new Image(width, height, pixels);
    }
}