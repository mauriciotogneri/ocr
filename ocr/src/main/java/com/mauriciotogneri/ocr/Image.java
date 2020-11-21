package com.mauriciotogneri.ocr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> visited = new HashSet<>();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                String coordinates = x + "-" + y;

                if (!visited.contains(coordinates))
                {
                    visited.add(coordinates);

                    Pixel pixel = pixel(x, y);

                    if (pixel.isBlack())
                    {
                        symbols.add(symbol(x, y, 1, 1, visited));
                    }
                }
            }
        }

        return symbols;
    }

    public Symbol symbol(int x, int y, int width, int height, Set<String> visited)
    {
        return new Symbol(x, y, new Matrix(width, height, new boolean[][] {{true}}));
    }
}