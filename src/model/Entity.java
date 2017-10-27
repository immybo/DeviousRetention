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

        if (getX() < 0) {
            this.x = 0;
        } else if (getX() + size > board.getWidth()) {
            this.x = board.getWidth() - size;
        }

        if (getY() < 0) {
            this.y = 0;
        } else if (getY() + size > board.getHeight()) {
            this.y = board.getHeight() - size;
        }
    }

    public void renderOn(Graphics g, CoordinateTranslation translation) {
        Point topLeft = translation.toScreenCoordinates(new Point.Double(getX(), getY()));
        Point size = new Point((int)(translation.getWorldToScreenMultiplier().x*getSize()),
                (int)(translation.getWorldToScreenMultiplier().y*getSize()));
        g.setColor(Color.WHITE);
        g.fillRect(topLeft.x, topLeft.y, size.x, size.y);
        g.setColor(Color.BLACK);
        g.drawRect(topLeft.x, topLeft.y, size.x, size.y);
        g.drawString(this.getClass().getCanonicalName(), topLeft.x + 10, topLeft.y + 50);
    }

    public Rectangle.Double getBounds() {
        return new Rectangle.Double(x, y, size, size);
    }

    public void tick(World world) {

    }

    public Color getPlayerColor() {
        return Color.BLACK;
    }
}
