package com.mauriciotogneri.ocr;

import java.io.File;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = Image.fromFile(new File("image.jpg"));

        Pixel pixel00 = image.pixel(0, 0);
        System.out.println(pixel00);
    }
}