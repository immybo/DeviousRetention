package model;

import controller.Action;
import model.entity.OwnedEntity;
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
     * the given board. If we would collide with any tiles,
     * "bounces" us back out of those collisions and returns true.
     * Otherwise, returns false.
     */
    public boolean moveBy(Board board, double x, double y) {
        boolean ret = false;

        // See if we'll collide with anything (and "bounce" back out of it if we would)
        double dX = x;
        double dY = y;
        double ddX = -x / 10;
        double ddY = -y / 10;
        int i = 0;
        boolean colliding = true;
        while (colliding && i < 10) {
            double newX = getX()+dX;
            double newY = getY()+dY;
            int top = (int)(newY-getSize()/2);
            int bottom = (int)(newY+getSize()/2);
            int left = (int)(newX-getSize()/2);
            int right = (int)(newX+getSize()/2);
            // Check the top-left, top-right, bottom-left, bottom-right for collisions
            Point topLeft = new Point(left, top);
            Point topRight = new Point(right, top);
            Point bottomLeft = new Point(left, bottom);
            Point bottomRight = new Point(right, bottom);

            if (board.getTile(topLeft).collides() || board.getTile(topRight).collides() ||
                    board.getTile(bottomLeft).collides() || board.getTile(bottomRight).collides()) {
                colliding = true;
                dX += ddX;
                dY += ddY;
                ret = true;
            } else {
                colliding = false;
            }

            i++;
        }

        moveBy(dX, dY);

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

        return ret;
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        Point topLeft = translation.toScreenCoordinates(new Point.Double(getX()-getSize()/2, getY()-getSize()/2));
        Point size = new Point((int)(translation.getWorldToScreenMultiplier().x*getSize()),
                (int)(translation.getWorldToScreenMultiplier().y*getSize()));
        Point bottomRight = new Point(topLeft.x+size.x, topLeft.y+size.y);
        g.setColor(Color.WHITE);
        g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
        g.setColor(Color.BLACK);
        g.drawRect(topLeft.x, topLeft.y, size.x, size.y);
        g.drawString(this.getClass().getCanonicalName(), topLeft.x + 10, topLeft.y + 50);

        if (this instanceof OwnedEntity) {
            OwnedEntity e = ((OwnedEntity)this);
            g.setColor(Color.RED);
            g.fillRect(topLeft.x, topLeft.y - 20, size.x, 15);
            g.setColor(Color.GREEN);
            g.fillRect(topLeft.x, topLeft.y - 20, (int)(size.x * e.getHealthProportion()), 15);
            g.setColor(Color.BLACK);
            g.drawRect(topLeft.x, topLeft.y - 20, size.x, 15);
        }
    }

    public Rectangle.Double getBounds() {
        return new Rectangle.Double(x-getSize()/2, y-getSize()/2, getSize(), getSize());
    }

    public void tick(World world) {

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

    /**
     * Returns the actions which this entity can perform. This actions
     * will be shown to the player as buttons when the entity is selected.
     * For example, a building automatically has actions to train all of
     * its trainable units.
     */
    public Action[] getActions() {
        return new Action[0];
    }
}
