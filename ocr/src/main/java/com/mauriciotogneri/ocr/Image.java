package com.mauriciotogneri.ocr;

import java.util.ArrayList;
import java.util.List;

public class Image
{
    public final int width;
    public final int height;
    public final int[][] pixels;

    public Image(int width, int height, int[][] pixels)
    {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Pixel pixel(int x, int y)
    {
        return new Pixel(pixels[x][y]);
    }

    public Image grayScale()
    {
        return transform(Pixel::grayScale);
    }

    public Image binarize()
    {
        return transform(Pixel::binarize);
    }

    public Image transform(Pixel.Transform transform)
    {
        int[][] pixels = new int[width][height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                Pixel pixel = pixel(x, y);
                pixels[x][y] = transform.transform(pixel).value;
            }
        }

        return new Image(width, height, pixels);
    }

    public List<Symbol> symbols()
    {
        List<Symbol> symbols = new ArrayList<>();

        boolean[][] matrix = new boolean[2][3];
        matrix[0] = new boolean[] {true, false, true};
        matrix[1] = new boolean[] {false, true, false};

        symbols.add(new Symbol(
                0,
                0,
                new Matrix(2, 3, matrix)
        ));

        return symbols;
    }
}