package model;

/**
 * Something on the layer above tiles, can be interacted with by other entities.
 */
public abstract class Entity {
    private double x;
    private double y;
    private double size;

    public Entity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
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
        } else if (getX() - size > board.getWidth() - 1) {
            this.x = board.getWidth() - 1 - size;
        }

        if (getY() < 0) {
            this.y = 0;
        } else if (getY() - size > board.getHeight() - 1) {
            this.y = board.getHeight() - 1 - size;
        }
    }
}