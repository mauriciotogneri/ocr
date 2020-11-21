package com.mauriciotogneri.orc.test;

import com.mauriciotogneri.ocr.Image;
import com.mauriciotogneri.ocr.Pixel;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageTest
{
    @Test
    public void example() throws IOException
    {
        Image image = Image.fromFile(new File("src/test/resources/test.png"));

        Pixel pixel00 = image.pixel(0, 0);
        assertEquals(255, pixel00.alpha);
        assertEquals(255, pixel00.red);
        assertEquals(0, pixel00.green);
        assertEquals(0, pixel00.blue);

        Pixel pixel01 = image.pixel(0, 1);
        assertEquals(255, pixel01.alpha);
        assertEquals(0, pixel01.red);
        assertEquals(255, pixel01.green);
        assertEquals(0, pixel01.blue);

        Pixel pixel10 = image.pixel(1, 0);
        assertEquals(255, pixel10.alpha);
        assertEquals(0, pixel10.red);
        assertEquals(0, pixel10.green);
        assertEquals(255, pixel10.blue);

        Pixel pixel11 = image.pixel(1, 1);
        assertEquals(100, pixel11.alpha);
        assertEquals(255, pixel11.red);
        assertEquals(0, pixel11.green);
        assertEquals(0, pixel11.blue);
    }
}