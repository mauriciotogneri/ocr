package com.mauriciotogneri.ocr;

import java.io.File;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Image image = ImageFile.load(new File("input/image.jpg"));
        List<Symbol> symbols = image.binarize().symbols();

        for (Symbol symbol : symbols)
        {
            ImageFile.save(symbol.image(), "png", new File("output/" + symbol.x + "-" + symbol.y + ".png"));
        }
        ImageFile.save(image.binarize(), "png", new File("output/image.png"));
    }
}