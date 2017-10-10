package model;

import java.awt.*;

/**
 * Represents the background tiles in the world.
 * These tiles are both cosmetic and can have collision properties.
 */
public class Board {
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void renderOn(Graphics g) {
        double tileWidth = 50; //(g.getClipBounds().width+0.0) / width;
        double tileHeight = 50; //(g.getClipBounds().height+0.0) / height;
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                // Tile tile = getTile(i, j);
                g.setColor(Color.GREEN);
                g.fillRect((int)(tileWidth*i), (int)(tileHeight*j), (int)tileWidth, (int)tileHeight);
                g.setColor(Color.BLACK);
                g.drawRect((int)(tileWidth*i), (int)(tileHeight*j), (int)tileWidth, (int)tileHeight);
            }
        }
    }
}
