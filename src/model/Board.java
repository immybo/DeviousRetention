package model;

import model.tile.GrassTile;
import util.CoordinateTranslation;

import java.awt.*;
import java.io.Serializable;

/**
 * Represents the background tiles in the world.
 * These tiles are both cosmetic and can have collision properties.
 */
public class Board implements Serializable {
    private final Tile[][] tiles;
    private final int width;
    private final int height;

    public Board(Tile[][] tiles) {
        this.tiles = tiles;
        width = tiles[0].length;
        height = tiles.length;
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
            throw new IllegalArgumentException("Position ("+x+","+y+") is invalid for a board of width " + getWidth() + " and height " + getHeight() + ".");
        }
        return tiles[y][x];
    }

    public Tile getTile(Point point) {
        return getTile(point.x, point.y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        Point size = translation.getWorldToScreenMultiplier();

        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                // Tile tile = getTile(i, j);
                Point topLeft = translation.toScreenCoordinates(new Point.Double(i, j));
                if (getTile(i,j) instanceof GrassTile)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.GRAY);
                g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
                g.setColor(Color.BLACK);
                g.drawRect(topLeft.x, topLeft.y, size.x, size.y);
            }
        }
    }
}
