package model;

import util.CoordinateTranslation;

import java.awt.*;
import java.io.Serializable;

/**
 * Something on the layer above tiles, can be interacted with by other entities.
 */
public abstract class Entity implements Serializable {
    private static int nextID = 0;

    public final int id;

    // Center position
    private double x;
    private double y;

    private double size;

    public Entity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;

        this.id = nextID;
        nextID++;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    private void moveBy(double x, double y) {
        this.x += x;
        this.y += y;
    }

    /**
     * Moves by a given amount, constrained to remain within
     * the given board.
     */
    public void moveBy(Board board, double x, double y) {
        moveBy(x, y);

        if (getX() < getSize()/2) {
            this.x = getSize()/2;
        } else if (getX() + getSize()/2 > board.getWidth()) {
            this.x = board.getWidth() - getSize()/2;
        }

        if (getY() < getSize()/2) {
            this.y = getSize()/2;
        } else if (getY() + getSize()/2 > board.getHeight()) {
            this.y = board.getHeight() - getSize()/2;
        }
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        Point topLeft = translation.toScreenCoordinates(new Point.Double(getX()-getSize()/2, getY()-getSize()/2));
        Point size = new Point((int)(translation.getWorldToScreenMultiplier().x*getSize()),
                (int)(translation.getWorldToScreenMultiplier().y*getSize()));
        g.setColor(Color.WHITE);
        g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
        g.setColor(Color.BLACK);
        g.drawRect(topLeft.x, topLeft.y, size.x, size.y);
        g.drawString(this.getClass().getCanonicalName(), topLeft.x + 10, topLeft.y + 50);
    }

    public Rectangle.Double getBounds() {
        return new Rectangle.Double(x-getSize()/2, y-getSize()/2, getSize(), getSize());
    }

    public void tick(World world) {

    }

    public Color getPlayerColor() {
        return Color.BLACK;
    }

    /**
     * Returns the straight-line distance between this and
     * another entity.
     */
    public double distanceTo(Entity other) {
        return Math.sqrt(Math.pow(getX()-other.getX(), 2) + Math.pow(getY()-other.getY(), 2));
    }

    public Point.Double vectorTo(Entity other) {
        return new Point.Double(getX()-other.getX(), getY()-other.getY());
    }

    /**
     * Returns the closest point from this entity, which is
     * within the given range of the given other entity.
     */
    public Point.Double closestPoint(Entity other, double range) {
        // Hahahahah good implementation :) In future use path finding with
        // termination criteria being within that range of other.
        return new Point.Double(other.getX() - range, other.getY());
    }
}
