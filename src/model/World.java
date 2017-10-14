package model;

import model.tile.GrassTile;
import util.CoordinateTranslation;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World implements Serializable {
    public static final World NULL_WORLD = new World(new Board(new Tile[][]{new Tile[]{new GrassTile()}}));

    private final Board board;
    private List<Entity> entities;

    public World(Board board) {
        this.board = board;
        this.entities = new ArrayList<Entity>();
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }

    public Entity[] getEntities() {
        return entities.toArray(new Entity[0]);
    }

    public Board getBoard() {
        return board;
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        board.renderOn(g, translation);
        for (Entity e : entities) {
            e.renderOn(g, translation);
        }
    }

    public void tick() {
        for (Entity e : entities) {
            e.tick(this);
        }
    }
}
