package com.mauriciotogneri.ocr;

import java.io.File;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = ImageFile.load(new File("image.jpg"));
        ImageFile.save(image.binarize(), new File("image2.jpg"));
    }
}