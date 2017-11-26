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

    private List<Entity> entitiesToAdd;
    private List<Integer> entitiesToRemove;

    public World(Board board) {
        this.board = board;
        this.entities = new ArrayList<Entity>();
        this.entitiesToAdd = new ArrayList<Entity>();
        this.entitiesToRemove = new ArrayList<Integer>();
    }

    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }
    public void removeEntity(int id) { entitiesToRemove.add(id); }
    public void removeEntity(Entity ent) { entitiesToRemove.add(ent.id); }

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
        for (Entity e : entitiesToAdd) {
            entities.add(e);
        }
        entitiesToAdd.clear();

        for (Integer entID : entitiesToRemove) {
            entities.remove(getEntityByID(entID));
        }
        entitiesToRemove.clear();

        for (Entity e : entities) {
            e.tick(this);
        }
    }

    public Entity getEntityByID(int id) {
        // Naiive for now; might not need improvement depending on where we use this
        for (Entity e : entities) {
            if (e.id == id) {
                return e;
            }
        }

        throw new IllegalArgumentException("Unable to find entity with ID " + id);
    }

    public Entity getEntityAt(Point.Double point) {
        // Just return the first one we find
        for (Entity e : entities) {
            if (e.getBounds().contains(point)) {
                return e;
            }
        }

        return null;
    }

    public Entity[] getEntitiesIn(Rectangle.Double rect) {
        List<Entity> ret = new ArrayList<Entity>();
        for (Entity e : entities) {
            if (e.getBounds().intersects(rect)) {
                ret.add(e);
            }
        }
        return ret.toArray(new Entity[0]);
    }
}
