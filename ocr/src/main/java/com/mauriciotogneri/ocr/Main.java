package com.mauriciotogneri.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = Image.fromFile(new File("image.jpg"));

        BufferedImage bufferedImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < image.width; x++)
        {
            for (int y = 0; y < image.height; y++)
            {
                bufferedImage.setRGB(x, y, image.pixel(x, y).value);
            }
        }

        FileOutputStream fileOutputStream = new FileOutputStream(new File("image2.jpg"));
        ImageIO.write(bufferedImage, "jpg", fileOutputStream);
    }
}