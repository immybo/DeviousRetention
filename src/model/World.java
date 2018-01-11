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

    private List<Player> players;

    public World(Board board) {
        this.board = board;
        this.entities = new ArrayList<Entity>();
        this.entitiesToAdd = new ArrayList<Entity>();
        this.entitiesToRemove = new ArrayList<Integer>();
        this.players = new ArrayList<Player>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }
    public Player getPlayer(int playerNumber) {
        for (Player p : players) {
            if (p.getPlayerNumber() == playerNumber) {
                return p;
            }
        }
        return null;
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

    public Entity[] getEntitiesAt(Point.Double point) {
        List<Entity> ents = new ArrayList<Entity>();
        for (Entity e : entities) {
            if (e.getBounds().contains(point)) {
                ents.add(e);
            }
        }
        return ents.toArray(new Entity[0]);
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

    /**
     * Returns whether or not the given entity would be colliding with anything
     * if it was at the given x and y coordinates.
     */
    public boolean isColliding(Entity entity, double x, double y) {
        boolean isColliding = false;

        int top = (int)(y-entity.getSize()/2);
        int bottom = (int)(y+entity.getSize()/2);
        int left = (int)(x-entity.getSize()/2);
        int right = (int)(x+entity.getSize()/2);
        // Check the top-left, top-right, bottom-left, bottom-right for collisions
        Point topLeft = new Point(left, top);
        Point topRight = new Point(right, top);
        Point bottomLeft = new Point(left, bottom);
        Point bottomRight = new Point(right, bottom);

        isColliding |= board.getTile(topLeft).collides();
        isColliding |= board.getTile(topRight).collides();
        isColliding |= board.getTile(bottomLeft).collides();
        isColliding |= board.getTile(bottomRight).collides();
        if (isColliding) {
            return true;
        }

        // This is pretty slow. There's probably a much better way to do it.
        for (Entity otherEntity : entities) {
            if (otherEntity == entity) continue;
            if (otherEntity.getX() + otherEntity.getSize()/2 > x - entity.getSize()/2 &&
                    otherEntity.getX() - otherEntity.getSize()/2 < x + entity.getSize()/2 &&
                    otherEntity.getY() + otherEntity.getSize()/2 > y - entity.getSize()/2 &&
                    otherEntity.getY() - otherEntity.getSize()/2 < y + entity.getSize()/2) {
                return true;
            }
        }

        return false;
    }
}
