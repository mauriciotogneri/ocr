package com.mauriciotogneri.ocr;

import java.io.File;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = ImageFile.load(new File("images/image.jpg"));
        List<Symbol> symbols = image.binarize().symbols();

        for (Symbol symbol : symbols)
        {
            ImageFile.save(symbol.image(), "png", new File(System.nanoTime() + ".png"));
        }
        //ImageFile.save(image.binarize(), "jpg", new File("images/image2.jpg"));
        //ImageFile.save(image.binarize(), "png", new File("images/image2.png"));
    }
}