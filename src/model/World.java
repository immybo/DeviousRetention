package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World {
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
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
    }
}
