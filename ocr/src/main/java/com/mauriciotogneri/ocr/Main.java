package com.mauriciotogneri.ocr;

import java.io.File;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = Image.fromFile(new File("image.jpg"));
        image.toFile(new File("image2.jpg"));
    }
}