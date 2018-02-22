package model;

import controller.Action;
import controller.TickObject;
import model.tile.GrassTile;
import util.CoordinateTranslation;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World implements Serializable {
    public static final double PATHFINDING_GRANULARITY = 0.25;

    public static final World NULL_WORLD = new World(new Board(new Tile[][]{new Tile[]{new GrassTile()}}));

    private final Board board;
    private List<Entity> entities;

    private List<Entity> entitiesToAdd;
    private List<Integer> entitiesToRemove;

    private List<Player> players;

    private Set<Entity>[][] entitiesBySquare;

    public World(Board board) {
        this.board = board;
        this.entities = new ArrayList<Entity>();
        this.entitiesToAdd = new ArrayList<Entity>();
        this.entitiesToRemove = new ArrayList<Integer>();
        this.players = new ArrayList<Player>();

        entitiesBySquare = new HashSet[board.getHeight()][board.getWidth()];
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
    public Player[] getPlayers() {
        return players.toArray(new Player[0]);
    }
    public void setPlayers(Player[] players) {
        this.players = Arrays.asList(players);
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

    public synchronized void setEntities(Entity[] entities) {
        this.entities = Arrays.asList(entities);
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

        groupEntitiesBySquare();

        for (Entity e : entities) {
            e.tick(this);
        }
    }

    private void groupEntitiesBySquare() {
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                entitiesBySquare[y][x] = new HashSet<Entity>();
            }
        }

        for (Entity e : entities) {
            for (Point p : containedTiles(e.getSize(), e.getX(), e.getY())) {
                entitiesBySquare[p.y][p.x].add(e);
            }
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

    private Point[] containedTiles(double size, double x, double y) {
        int left = (int)(x-size/2);
        int top = (int)(y-size/2);
        int right = (int)(x+size/2);
        int bottom = (int)(y+size/2);

        java.util.List<Point> contained = new ArrayList<Point>();
        // <= To include partially contained tiles
        for (int checkX = left; checkX <= right; checkX++) {
            for (int checkY = top; checkY <= bottom; checkY++) {
                contained.add(new Point(checkX, checkY));
            }
        }
        return contained.toArray(new Point[0]);
    }

    /**
     * Returns whether or not something with the given size would be colliding with anything
     * if it was at the given x and y coordinates.
     */
    public boolean isColliding(double size, double x, double y) {
        return isColliding(size, x, y, null);
    }

    public boolean isColliding(Entity e, double x, double y) {
        return isColliding(e.getSize(), x, y, e);
    }

    private boolean isColliding(double size, double x, double y, Entity excluded) {
        Rectangle.Double bounds = new Rectangle.Double(x-size/2, y-size/2, size, size);
        Point[] containedTiles = containedTiles(size, x, y);
        for (Point toCheck : containedTiles) {
            Set<Entity> potentialCollisions = entitiesBySquare[toCheck.y][toCheck.x];
            for (Entity potentialCollision : potentialCollisions) {
                if (potentialCollision == excluded) continue;
                else if (potentialCollision.getBounds().intersects(bounds)) {
                    return true;
                }
            }

            if (getBoard().getTile(toCheck.x, toCheck.y).collides()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns (roughly) the nearest point to the given point which has no entities
     * or collidable terrain within size/2 squares, or null if there was none found
     * within the given maximum range.
     */
    public Point.Double getNearestEmptyPoint(double size, double x, double y, double maxRange) {
        double bestRange = Double.MAX_VALUE;
        Point.Double bestPoint = null;

        for (double cX = x - maxRange; cX < x + maxRange; cX += PATHFINDING_GRANULARITY) {
            for (double cY = y - maxRange; cY < y + maxRange; cY += PATHFINDING_GRANULARITY) {
                if (cX-size/2 < 0 || cY-size/2 < 0 || cX+size/2 >= getBoard().getWidth() || cY+size/2 >= getBoard().getHeight()) {
                    continue;
                }

                if (!isColliding(size, cX, cY)) {
                    Point.Double pt = new Point.Double(cX, cY);
                    double range = pt.distance(x, y);
                    if (range < bestRange) {
                        bestRange = range;
                        bestPoint = pt;
                    }
                }
            }
        }

        return bestPoint;
    }

    /**
     * Returns a set of points representing a path from the given start point
     * to the given end point. The points are inclusive of startPoint and endPoint.
     *
     * Note that this assumes there is a valid path, and that endPoint is a valid
     * position.
     *
     * For detecting collisions, the given size is used, with the current point being
     * the center.
     *
     * The given range is the maximum distance from the given endPoint at which the final
     * point will be. If it's -1, it will be set to a reasonable default.
     */
    public Point.Double[] getPath(Point.Double startPoint, Point.Double endPoint, Entity entity, double range) {
        if (range == -1) {
            range = PATHFINDING_GRANULARITY*2;
        }

        Point.Double startPointMiddle = new Point.Double((int)startPoint.x + PATHFINDING_GRANULARITY, (int)startPoint.y + PATHFINDING_GRANULARITY);
        Point.Double endPointReal = getNearestEmptyPoint(entity.getSize(), endPoint.x, endPoint.y, 3);
        if (endPointReal == null) {
            // We couldn't find an empty space, so I guess just don't move anywhere
            return new Point.Double[0];
        }

        // Yeah we're defining a class inside a method. Deal with it.
        class AStarNode implements Comparable<AStarNode> {
            AStarNode(Point.Double point, AStarNode from, double costTo) {
                this.point = point;
                this.from = from;
                this.costTo = costTo;
                this.heuristicCost = point.distance(endPointReal);
            }

            Point.Double point;
            AStarNode from;
            double costTo;
            double heuristicCost;

            @Override
            public int compareTo(AStarNode o) {
                double diff = (this.costTo + this.heuristicCost) - (o.costTo + o.heuristicCost);
                return diff < 0 ? -1 : diff == 0 ? 0 : 1;
            }
        }

        // We do this on the granularity of tiles for simplicity and speed

        // A*
        Set<Point.Double> visited = new HashSet<Point.Double>();
        java.util.Queue<AStarNode> fringe = new PriorityQueue<AStarNode>();

        fringe.add(new AStarNode(startPointMiddle, null, 0));
        while (!fringe.isEmpty()) {
            AStarNode current = fringe.poll();
            if (visited.contains(current.point)) {
                continue;
            }

            if (current.point.distance(endPointReal) < range) {
                List<Point.Double> points = new ArrayList<Point.Double>();
                while (current.from != null) {
                    points.add(current.point);
                    current = current.from;
                }
                Collections.reverse(points);
                return points.toArray(new Point.Double[0]);
            }

            visited.add(current.point);

            Point.Double[] neighbourPoints = new Point.Double[] {
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y),
                    new Point.Double(current.point.x, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x, current.point.y - PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y - PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y - PATHFINDING_GRANULARITY)
            };

            for (Point.Double neighbourPoint : neighbourPoints) {
                if (neighbourPoint.x - entity.getSize()/2 < 0 || neighbourPoint.y - entity.getSize()/2 < 0 || neighbourPoint.x + entity.getSize()/2 >= board.getWidth() || neighbourPoint.y + entity.getSize()/2 >= board.getHeight()) {
                    continue;
                } else if (isColliding(entity, neighbourPoint.x, neighbourPoint.y)) {
                    continue;
                } else if (visited.contains(neighbourPoint)) {
                    continue;
                }

                AStarNode neighbour = new AStarNode(neighbourPoint, current, current.costTo + current.point.distance(neighbourPoint));
                fringe.add(neighbour);
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof World && o.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        // Although it's not strictly the purpose of a hash code, we don't actually
        // need to compare everything here - only things that could be out of sync
        // between clients. Since the board can't change within a game, we don't
        // need to check that, for example.

        int hashCode = 0;

        for (Entity e : getEntities()) {
            hashCode += e.hashCode();
        }
        // Apparently multiplying is good for uniqueness, so
        hashCode *= Math.pow(getEntities().length, 2);

        return hashCode;
    }
}
