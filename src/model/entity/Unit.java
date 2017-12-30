package model.entity;

import model.Entity;
import model.World;

import java.awt.*;
import java.lang.reflect.Constructor;

/**
 * An entity which is owned by a player, which can be moved around and can perform actions
 * such as attacking or healing.
 */
public abstract class Unit extends OwnedEntity {
    private Point.Double movePoint = null;
    private OwnedEntity target = null;
    private double movementSpeed;
    private double range;
    private double attackCounter;
    private double attackTime = 10;
    private int damage;

    public Unit(double x, double y, double size, double range, int damage, int maxHealth, int playerNumber, double movementSpeed) {
        super(x, y, size, playerNumber, maxHealth);
        this.movementSpeed = movementSpeed;
        this.range = range;
        this.damage = damage;
    }

    /**
     * Sets the point for this unit to start moving towards.
     * Every tick, this unit will move by its movespeed towards
     * this point until it gets close enough.
     */
    public void setMovePoint(Point.Double newPoint) {
        clearTarget();
        setMovePointNoCancel(newPoint);
    }

    private void setMovePointNoCancel(Point.Double newPoint) {
        this.movePoint = newPoint;
    }

    public void clearMovePoint() {
        this.movePoint = null;
    }

    public void setTarget(OwnedEntity target) {
        clearMovePoint();
        this.target = target;
        this.attackCounter = 0;
    }

    public void clearTarget() { this.target = null; }

    @Override
    public void tick(World world) {
        super.tick(world);

        // A unit can both move and attack every tick, but ordering it
        // to do either clears the other one. This is used for when
        // we're moving towards our target.

        if (movementSpeed != 0 && movePoint != null) {
            moveTick(world);
        }

        if (target != null) {
            attackTick(world);
        }
    }

    private void attackTick(World world) {
        double distance = this.distanceTo(target);
        if (distance < range) {
            // Move to be within range
            setMovePointNoCancel(closestPoint(target, range));
            return;
        }

        clearMovePoint(); // Since we may have been moving towards our target
        if (attackCounter < attackTime) {
            attackCounter++;
        } else {
            attackCounter = 0;
            performAttack(target);
        }
    }

    private void performAttack(OwnedEntity target) {
        target.changeHealth(-this.damage);
    }

    private void moveTick(World world) {
        double moveX = movePoint.x - this.getX();
        double moveY = movePoint.y - this.getY();

        double distance = Math.sqrt(moveX*moveX + moveY*moveY);
        if (distance <= movementSpeed) {
            this.moveBy(world.getBoard(), moveX, moveY);
            clearMovePoint();
            return;
        }

        double deltaX = moveX/(Math.abs(moveX)+Math.abs(moveY)) * movementSpeed;
        double deltaY = moveY/(Math.abs(moveX)+Math.abs(moveY)) * movementSpeed;

        boolean collided = this.moveBy(world.getBoard(), deltaX, deltaY);
        if (collided) {
            this.movePoint = null;
        }
    }

    public boolean canAttack(Entity other) {
        if (!(other instanceof OwnedEntity)) {
            return false;
        } else if (((OwnedEntity)other).getPlayerNumber() == getPlayerNumber()) {
            return false;
        } else {
            return true;
        }
    }
}
