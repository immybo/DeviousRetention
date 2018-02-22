package model;

import controller.Action;
import model.entity.OwnedEntity;
import util.CoordinateTranslation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Something on the layer above tiles, can be interacted with by other entities.
 */
public abstract class Entity implements Serializable {
    public enum Ability {
        ATTACK,
        GATHER
    }

    private static int nextID = 0;

    public final int id;

    // Center position
    private double x;
    private double y;

    private double size;

    private String imageName;
    private transient Image image = null;

    private final String name;

    public Entity(double x, double y, double size, String imageName, String name) {
        this.x = x;
        this.y = y;
        this.size = size;

        this.id = nextID;
        nextID++;

        this.imageName = imageName;
        this.name = name;
    }

    public Entity(double x, double y, double size, String name) {
        this.x = x;
        this.y = y;
        this.size = size;

        this.id = nextID;
        nextID++;

        this.imageName = null;
        this.name = name;
    }

    public static void setNextID(int id) {
        nextID = id;
    }

    public static int getNextID() {
        return nextID;
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
     * the given board. If we would collide with anything,
     * "bounces" us back out of those collisions and returns true.
     * Otherwise, returns false.
     */
    public boolean moveBy(World world, double x, double y) {
        boolean ret = false;

        // See if we'll collide with anything (and "bounce" back out of it if we would)
        double dX = x;
        double dY = y;
        double ddX = -x / 10;
        double ddY = -y / 10;
        int i = 0;
        boolean colliding = true;

        while (colliding && i <= 15) {
            double newX = getX() + dX;
            if (world.isColliding(this, newX, this.getY())) {
                colliding = true;
                dX += ddX;
                ret = true;
            } else {
                colliding = false;
            }
        }

        i = 0;
        colliding = true;
        while (colliding && i <= 15) {
            double newY = getY() + dY;
            if (world.isColliding(this, this.getX()+dX, newY)) {
                colliding = true;
                dY += ddY;
                ret = true;
            } else {
                colliding = false;
            }
        }

        moveBy(dX, dY);

        if (getX() < getSize()/2) {
            this.x = getSize()/2;
        } else if (getX() + getSize()/2 > world.getBoard().getWidth()) {
            this.x = world.getBoard().getWidth() - getSize()/2;
        }

        if (getY() < getSize()/2) {
            this.y = getSize()/2;
        } else if (getY() + getSize()/2 > world.getBoard().getHeight()) {
            this.y = world.getBoard().getHeight() - getSize()/2;
        }

        return ret;
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        Point topLeft = translation.toScreenCoordinates(new Point.Double(getX()-getSize()/2, getY()-getSize()/2));
        Point size = new Point((int)(translation.getWorldToScreenMultiplier().x*getSize()),
                (int)(translation.getWorldToScreenMultiplier().y*getSize()));
        Point bottomRight = new Point(topLeft.x+size.x, topLeft.y+size.y);

        if (this.imageName == null) {
            g.setColor(Color.WHITE);
            g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
            g.setColor(Color.BLACK);
            g.drawRect(topLeft.x, topLeft.y, size.x, size.y);
            g.drawString(this.getClass().getCanonicalName(), topLeft.x + 10, topLeft.y + 50);
        } else {
            if (this.image == null) {
                try {
                    this.image = ImageIO.read(new File("res/sprite/" + imageName));
                } catch (IOException e ){
                    System.err.println("Couldn't load image " + imageName);
                    this.imageName = null;
                }
            }
            g.drawImage(image, topLeft.x, topLeft.y, size.x, size.y, null);
        }

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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Entity && o.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode *= getX();
        hashCode *= getY();
        hashCode *= getName().hashCode();
        return hashCode;
    }
}
