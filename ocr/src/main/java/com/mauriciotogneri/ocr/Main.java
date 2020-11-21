package com.mauriciotogneri.ocr;

import java.io.File;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = ImageFile.load(new File("image.jpg"));
        ImageFile.save(image.binarize(), "jpg", new File("image2.jpg"));
        ImageFile.save(image.binarize(), "png", new File("image2.png"));
    }
}