package com.mauriciotogneri.ocr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        int sum = 0;
        Map<Integer, Integer> map = new HashMap<>();

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                Pixel pixel = pixel(x, y);
                int value = pixel.average();
                sum += value;

                if (map.containsKey(value))
                {
                    map.put(value, map.get(value) + 1);
                }
                else
                {
                    map.put(value, 1);
                }
            }
        }

        StringBuilder builder = new StringBuilder();

        for (Integer a : map.keySet())
        {
            builder.append(a);
            builder.append(";");
            builder.append(map.get(a));
            builder.append("\n");
        }

        int average = sum / (width * height);

        return transform(pixel -> pixel.binarize(165));
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
        int sumSizes = 0;

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
                        sumSizes += symbol.size();
                        symbols.add(symbol);
                    }
                }
            }
        }

        int averageSize = sumSizes / symbols.size();
        int sqrt = (int) Math.sqrt(averageSize);

        return symbols
                .stream()
                .filter(symbol -> (symbol.height >= sqrt) || (symbol.width >= sqrt))
                .collect(Collectors.toList());
    }

    public List<Position> positions(Position position, Set<Position> visited)
    {
        List<Position> positions = new ArrayList<>();

        if ((position.x >= 0) &&
                (position.x < width) &&
                (position.y >= 0) &&
                (position.y < height) &&
                pixel(position.x, position.y).isBlack() &&
                !visited.contains(position))
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