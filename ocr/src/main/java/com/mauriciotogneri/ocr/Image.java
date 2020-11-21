package com.mauriciotogneri.ocr;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image
{
    public final int width;
    public final int height;
    public final int[] pixels;

    public Image(int width, int height, int[] pixels)
    {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Pixel pixel(int i, int j)
    {
        return new Pixel(pixels[(j * height) + i]);
    }

    @NotNull
    public static Image fromFile(File file) throws IOException
    {
        BufferedImage image = ImageIO.read(file);

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                pixels[(j * height) + i] = image.getRGB(i, j);
            }
        }

        return new Image(width, height, pixels);
    }
}