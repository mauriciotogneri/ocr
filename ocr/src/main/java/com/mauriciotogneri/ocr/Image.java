package com.mauriciotogneri.ocr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Image
{
    public final int width;
    public final int height;
    public final int[][] pixels;

    public Image(int width, int height, int[][] pixels)
    {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public Pixel pixel(int x, int y)
    {
        return new Pixel(pixels[x][y]);
    }

    public Image grayScale()
    {
        return transform(Pixel::grayScale);
    }

    public Image binarize()
    {
        return transform(Pixel::binarize);
    }

    public Image transform(Pixel.Transform transform)
    {
        int[][] pixels = new int[width][height];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                Pixel pixel = pixel(x, y);
                pixels[x][y] = transform.transform(pixel).value;
            }
        }

        return new Image(width, height, pixels);
    }

    public List<Symbol> symbols()
    {
        List<Symbol> symbols = new ArrayList<>();
        Set<Position> visited = new HashSet<>();

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                Position position = new Position(x, y);

                if (!visited.contains(position))
                {
                    List<Position> positions = positions(position, visited);

                    if (!positions.isEmpty())
                    {
                        Symbol symbol = Symbol.fromPositions(positions);
                        symbols.add(symbol);
                    }
                }
            }
        }

        return symbols;
    }

    public List<Position> positions(Position position, Set<Position> visited)
    {
        List<Position> positions = new ArrayList<>();

        if (pixel(position.x, position.y).isBlack() && !visited.contains(position))
        {
            visited.add(position);
            positions.add(position);

            positions.addAll(positions(position.move(0, -1), visited));
            positions.addAll(positions(position.move(1, -1), visited));
            positions.addAll(positions(position.move(1, 0), visited));
            positions.addAll(positions(position.move(1, 1), visited));
            positions.addAll(positions(position.move(0, 1), visited));
            positions.addAll(positions(position.move(-1, 1), visited));
            positions.addAll(positions(position.move(-1, 0), visited));
            positions.addAll(positions(position.move(-1, -1), visited));
        }

        return positions;
    }
}