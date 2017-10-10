package model;

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
}
