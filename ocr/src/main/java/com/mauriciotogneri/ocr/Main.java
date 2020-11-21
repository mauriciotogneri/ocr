package com.mauriciotogneri.ocr;

import java.io.File;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = ImageFile.load(new File("image.jpg"));
        List<Symbol> symbols = image.symbols();

        for (Symbol symbol : symbols)
        {
            ImageFile.save(symbol.image(), "png", new File(System.currentTimeMillis() + ".png"));
        }
        //ImageFile.save(image.binarize(), "jpg", new File("image2.jpg"));
        //ImageFile.save(image.binarize(), "png", new File("image2.png"));
    }
}