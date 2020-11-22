package com.mauriciotogneri.ocr;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageFile
{
    @NotNull
    public static Image load(File file) throws IOException
    {
        BufferedImage image = ImageIO.read(file);

        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                pixels[x + (y * width)] = image.getRGB(x, y);
            }
        }

        return new Image(width, height, pixels);
    }

    public static void save(Image image, String format, File file) throws IOException
    {
        BufferedImage bufferedImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < image.width; x++)
        {
            for (int y = 0; y < image.height; y++)
            {
                bufferedImage.setRGB(x, y, image.pixel(x, y).value);
            }
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ImageIO.write(bufferedImage, format, fileOutputStream);
    }
}
