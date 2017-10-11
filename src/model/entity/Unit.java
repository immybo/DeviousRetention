package model.entity;

import model.World;

import java.awt.*;

/**
 * An entity which is owned by a player, which can be moved around and can perform actions
 * such as attacking or healing.
 */
public abstract class Unit extends OwnedEntity {
    private Point.Double movePoint = null;
    private double movementSpeed;

    public Unit(double x, double y, double size, int playerNumber, double movementSpeed) {
        super(x, y, size, playerNumber);
        this.movementSpeed = movementSpeed;
    }

    /**
     * Sets the point for this unit to start moving towards.
     * Every tick, this unit will move by its movespeed towards
     * this point until it gets close enough.
     */
    public void setMovePoint(Point.Double newPoint) {
        this.movePoint = newPoint;
    }

    public void clearMovePoint() {
        this.movePoint = null;
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        if (movementSpeed == 0 || movePoint == null) {
            return;
        }

        double moveX = movePoint.x - this.getX();
        double moveY = movePoint.y - this.getY();

        double distance = Math.sqrt(moveX*moveX + moveY*moveY);
        if (distance <= movementSpeed) {
            this.moveBy(world.getBoard(), moveX, moveY);
            return;
        }

        double deltaX = moveX/(moveX+moveY) * movementSpeed;
        double deltaY = moveY/(moveX+moveY) * movementSpeed;
        this.moveBy(world.getBoard(), deltaX, deltaY);
    }
}
