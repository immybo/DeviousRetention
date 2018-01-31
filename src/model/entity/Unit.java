package model.entity;

import model.Entity;
import model.Player;
import model.World;
import util.CoordinateTranslation;

import java.awt.*;
import java.lang.reflect.Constructor;

/**
 * An entity which is owned by a player, which can be moved around and can perform actions
 * such as attacking or healing.
 */
public class Unit extends OwnedEntity {
    private Point.Double movePoint = null;
    private Point.Double[] movePointSteps = null;
    private int currentMovePointStep = 0;
    private OwnedEntity target = null;
    private Resource gatherTarget = null;
    private double movementSpeed;
    private double range;
    private double attackCounter;
    private double attackTime = 10;
    private int damage;
    private Entity.Ability[] abilities;

    public Unit(double x, double y, double size, double range, int damage, int maxHealth, int playerNumber, double movementSpeed, Entity.Ability[] abilities) {
        super(x, y, size, playerNumber, maxHealth);
        this.movementSpeed = movementSpeed;
        this.range = range;
        this.damage = damage;
        this.abilities = abilities;
    }

    public boolean can(Entity.Ability doThis) {
        for (Entity.Ability a : abilities) {
            if (doThis == a) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the point for this unit to start moving towards.
     * Every tick, this unit will move by its movespeed towards
     * this point until it gets close enough.
     */
    public void setMovePoint(Point.Double newPoint) {
        clearAllTargets();
        setMovePointNoCancel(newPoint);
    }

    private void setMovePointNoCancel(Point.Double newPoint) {
        this.movePoint = newPoint;
    }

    public void setTarget(OwnedEntity target) {
        if (can(Ability.ATTACK)) {
            clearAllTargets();
            this.target = target;
            this.attackCounter = 0;
        }
    }

    public void clearAllTargets() {
        this.target = null;
        this.gatherTarget = null;
        this.movePoint = null;
        this.movePointSteps = null;
    }

    public void setGatherTarget(Resource target) {
        if (can(Ability.GATHER)) {
            clearAllTargets();
            this.gatherTarget = target;
        }
    }

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

        if (gatherTarget != null) {
            gatherTick(world);
        }
    }

    private void attackTick(World world) {
        double distance = this.distanceTo(target);
        if (distance > range) {
            // Move to be within range
            setMovePointNoCancel(closestPoint(target, range));
            return;
        }

        this.movePoint = null; // Since we may have been moving towards our target
        if (attackCounter < attackTime) {
            attackCounter++;
        } else {
            attackCounter = 0;
            performAttack(target);
        }
    }

    private void gatherTick(World world) {
        double distance = this.distanceTo(gatherTarget);
        double maxRange = gatherTarget.getSize()/2 + this.getSize()/2 + 0.5;
        if (distance > maxRange) {
            if (this.movePoint == null) {
                this.currentMovePointStep = 0;
            }
            // Move to be within range
            setMovePointNoCancel(new Point.Double(gatherTarget.getX(), gatherTarget.getY()));
            this.movePointSteps = world.getPath(new Point.Double(this.getX(), this.getY()), movePoint, this, maxRange-0.5);
            if (this.movePointSteps == null) {
                // We have no path so just try and go straight there
                this.movePointSteps = new Point.Double[]{ new Point.Double(movePoint.getX(), movePoint.getY()) };
            }
            return;
        }

        this.movePoint = null; // Since we may have been moving towards our target

        Player p = world.getPlayer(this.getPlayerNumber());
        if (gatherTarget.getRemainingCredits() >= gatherTarget.getEfficiency()) {
            p.earnCredits((int)gatherTarget.getEfficiency());
        } else {
            gatherTarget.takeCredits((int)gatherTarget.getEfficiency());
            p.earnCredits(gatherTarget.getRemainingCredits());
            gatherTarget.takeCredits(gatherTarget.getRemainingCredits());
        }
    }

    private void performAttack(OwnedEntity target) {
        target.changeHealth(-this.damage);
    }

    private void moveTick(World world) {
        // If we haven't done any path finding yet, do it now.
        if (this.movePointSteps == null) {
            this.movePointSteps = world.getPath(new Point.Double(this.getX(), this.getY()), movePoint, this, -1);
            if (this.movePointSteps == null) {
                // We have no path so just try and go straight there
                this.movePointSteps = new Point.Double[]{ new Point.Double(movePoint.getX(), movePoint.getY()) };
            }
            this.currentMovePointStep = 0;
        }

        if (this.currentMovePointStep >= this.movePointSteps.length) {
            clearAllTargets();
            return;
        }

        Point.Double movePoint = this.movePointSteps[this.currentMovePointStep];

        double moveX = movePoint.x - this.getX();
        double moveY = movePoint.y - this.getY();

        double distance = Math.sqrt(moveX*moveX + moveY*moveY);
        if (distance <= movementSpeed) {
            this.moveBy(world, moveX, moveY);
            this.currentMovePointStep++;
            return;
        }

        double deltaX = moveX/(Math.abs(moveX)+Math.abs(moveY)) * movementSpeed;
        double deltaY = moveY/(Math.abs(moveX)+Math.abs(moveY)) * movementSpeed;

        boolean collided = this.moveBy(world, deltaX, deltaY);
        if (collided) {
            this.movePointSteps = null; // Recalculate the path next time
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

    public double getRange() {
        return range;
    }
}
