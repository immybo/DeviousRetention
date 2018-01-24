package model;

import model.tile.GrassTile;
import util.CoordinateTranslation;

import java.awt.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World implements Serializable {
    public static final double PATHFINDING_GRANULARITY = 1;

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
     * Returns whether or not something with the given size would be colliding with anything
     * if it was at the given x and y coordinates.
     */
    public boolean isColliding(double size, double x, double y) {
        boolean isColliding = false;

        int top = (int)(y-size/2);
        int bottom = (int)(y+size/2);
        int left = (int)(x-size/2);
        int right = (int)(x+size/2);

        if (bottom >= getBoard().getHeight() ||
                top <= 0 ||
                right >= getBoard().getWidth() ||
                left <= 0) {
            return true;
        }

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
            double xDistance = Math.abs(otherEntity.getX() - x);
            double yDistance = Math.abs(otherEntity.getY() - y);
            double avgSize = otherEntity.getSize()/2 + size/2;
            if (xDistance < avgSize && yDistance < avgSize) {
                return true;
            }
        }

        return false;
    }

    public boolean isColliding(Entity e, double x, double y) {
        boolean isColliding = false;

        int top = (int)(y-e.getSize()/2);
        int bottom = (int)(y+e.getSize()/2);
        int left = (int)(x-e.getSize()/2);
        int right = (int)(x+e.getSize()/2);

        if (bottom >= getBoard().getHeight() ||
                top <= 0 ||
                right >= getBoard().getWidth() ||
                left <= 0) {
            return true;
        }

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
            if (otherEntity == e) continue;
            double xDistance = Math.abs(otherEntity.getX() - x);
            double yDistance = Math.abs(otherEntity.getY() - y);
            double size = otherEntity.getSize()/2 + e.getSize()/2;
            if (xDistance < size && yDistance < size) {
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

    private AStarNode[] getSuccessors(Entity e, AStarNode current, AStarNode start, AStarNode end) {
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

        List<AStarNode> successors = new ArrayList<AStarNode>();

        for (Point.Double neighbour : neighbourPoints) {
            successors.add(new AStarNode(neighbour, end.point, current, start.costTo + neighbour.distance(current.point)));

            double dX = Math.abs(neighbour.x - current.point.x);
            double dY = Math.abs(neighbour.y - current.point.y);

            AStarNode jumpPoint = jump(e, neighbour.x, neighbour.y, dX, dY, start.costTo, start, end);
            if (jumpPoint != null) successors.add(jumpPoint);
        }

        return successors.toArray(new AStarNode[0]);
    }

    private AStarNode jump(Entity e, double cX, double cY, double dX, double dY, double costTo, AStarNode start, AStarNode end) {
        double nextX = cX + dX;
        double nextY = cY + dY;

        if (this.isColliding(e, nextX, nextY)) return null;

        costTo = costTo + Math.sqrt(dX*dX + dY*dY);
        AStarNode retNode = new AStarNode(new Point.Double(nextX, nextY), end.point, start, start.costTo);

        if (dX != 0) {
            // Are there blocked nodes above or below us?
            // If there are, we have to stop the "jump".
            if (this.isColliding(e.getSize(), nextX, nextY - PATHFINDING_GRANULARITY) ||
                    this.isColliding(e.getSize(), nextX, nextY + PATHFINDING_GRANULARITY)) {
                return retNode;
            }
        } else {
            // Similarly, but look for blocked nodes horizontally
            if (this.isColliding(e.getSize(), nextX - PATHFINDING_GRANULARITY, nextY) ||
                    this.isColliding(e.getSize(), nextX + PATHFINDING_GRANULARITY, nextY)) {
                return retNode;
            }
        }

        /*
        // If we're diagonal, we also have to look at the other immediately adjacent blocks
        if (dX != 0 && dY != 0) {
            if (this.isColliding(e.getSize(), nextX - PATHFINDING_GRANULARITY, nextY - PATHFINDING_GRANULARITY) ||
                    this.isColliding(e.getSize(), nextX + PATHFINDING_GRANULARITY, nextY + PATHFINDING_GRANULARITY)) {

            }
        }*/

        // We can continue on forwards...
        return jump(e, nextX, nextY, dX, dY, costTo, start, end);
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
        Point.Double endPointMiddle = new Point.Double((int)endPoint.x + PATHFINDING_GRANULARITY, (int)endPoint.y + PATHFINDING_GRANULARITY);

        // We do this on the granularity of tiles for simplicity and speed

        // A*
        List<AStarNode> visited = new ArrayList<AStarNode>();
        java.util.Queue<AStarNode> fringe = new PriorityQueue<AStarNode>();

        AStarNode endPointNode = new AStarNode(endPoint, endPoint, null, 0);
        AStarNode startPointNode = new AStarNode(startPointMiddle, endPoint, null, 0);
        fringe.add(startPointNode);

        long start = System.currentTimeMillis();
        int ticks = 0;
        while (!fringe.isEmpty()) {
            ticks++;
            AStarNode current = fringe.poll();
            if (current.point.distance(endPointMiddle) < range) {
                List<Point.Double> points = new ArrayList<Point.Double>();
                while (current.from != null) {
                    points.add(current.point);
                    current = current.from;
                }
                Collections.reverse(points);

                System.out.println("Took " + ticks + " ticks.");
                System.out.println(System.currentTimeMillis() - start);

                return points.toArray(new Point.Double[0]);
            }

            visited.add(current);

            /*
            Point.Double[] neighbourPoints = new Point.Double[] {
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y),
                    new Point.Double(current.point.x, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x, current.point.y - PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y + PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x + PATHFINDING_GRANULARITY, current.point.y - PATHFINDING_GRANULARITY),
                    new Point.Double(current.point.x - PATHFINDING_GRANULARITY, current.point.y - PATHFINDING_GRANULARITY)
            };*/

            AStarNode[] neighbours = getSuccessors(entity, current, startPointNode, endPointNode);

            for (AStarNode neighbour : neighbours) {
                Point.Double neighbourPoint = neighbour.point;
                if (neighbourPoint.x - entity.getSize()/2 < 0 || neighbourPoint.y - entity.getSize()/2 < 0 || neighbourPoint.x + entity.getSize()/2 >= board.getWidth() || neighbourPoint.y + entity.getSize()/2 >= board.getHeight()) {
                    continue;
                }

                if (isColliding(entity, neighbourPoint.x, neighbourPoint.y)) {
                    continue;
                }

                if (visited.contains(neighbour)) {
                    continue;
                }

                // Two nodes are equal if  they share the same point, so...
                if (fringe.contains(neighbour)) {
                    for (AStarNode otherNeighbour : fringe) {
                        if (otherNeighbour.equals(neighbour)) {
                            // Only replace it if our path is shorter
                            if (neighbour.costTo < otherNeighbour.costTo) {
                                fringe.remove(otherNeighbour);
                                fringe.add(neighbour);
                            }
                            break;
                        }
                    }
                } else {
                    fringe.add(neighbour);
                }
            }
        }

        return null;
    }

    // Yeah we're defining a class inside a method. Deal with it.
    private class AStarNode implements Comparable<AStarNode> {
        AStarNode(Point.Double point, Point.Double endPoint, AStarNode from, double costTo) {
            this.point = point;
            this.from = from;
            this.costTo = costTo;
            this.heuristicCost = point.distance(endPoint);
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

        @Override
        public boolean equals(Object other) {
            if (other instanceof AStarNode) {
                if (((AStarNode)other).point.equals(this.point)) {
                    return true;
                }
            }
            return false;
        }
    }
}
