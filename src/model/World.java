package model;

import model.tile.GrassTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World {
    public static final World NULL_WORLD = new World(new Board(new Tile[][]{new Tile[]{new GrassTile()}}));

    private final Board board;
    private List<Entity> entities;

    public World(Board board) {
        this.board = board;
        this.entities = new ArrayList<Entity>();
    }

    public Board getBoard() {
        return board;
    }

    public void renderOn(Graphics g) {
        board.renderOn(g);
    }
}
