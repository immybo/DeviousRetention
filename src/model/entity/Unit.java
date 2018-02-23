package model.entity;

import model.Entity;
import model.Player;
import model.World;
import util.CoordinateTranslation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An entity which is owned by a player, which can be moved around and can perform actions
 * such as attacking or healing.
 */
public class Unit extends OwnedEntity {
    private Point.Double movePoint = null;
    private AtomicReference<Point.Double[]> movePointSteps;
    private boolean mustRecalculateSteps = false;
    private int currentMovePointStep = 0;
    private OwnedEntity target = null;
    private Resource gatherTarget = null;
    private double movementSpeed;
    private double range;
    private double attackCounter;
    private double attackTime = 10;
    private int damage;
    private Entity.Ability[] abilities;

    public Unit(World world, double x, double y, double size, double range, int damage, int maxHealth, int playerNumber, double movementSpeed, Entity.Ability[] abilities, String imageName, String name) {
        super(world, x, y, size, playerNumber, maxHealth, imageName, name);
        this.movementSpeed = movementSpeed;
        this.range = range;
        this.damage = damage;
        this.abilities = abilities;
        this.movePointSteps = new AtomicReference<Point.Double[]>(null);
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
        this.mustRecalculateSteps = true;
        this.movePoint = newPoint;
    }

    public void setTarget(OwnedEntity target) {
        if (can(Ability.ATTACK)) {
            clearAllTargets();
            this.target = target;
            this.attackCounter = 0;
        }
    }

    public void setGatherTarget(Resource target) {
        if (can(Ability.GATHER)) {
            clearAllTargets();
            this.gatherTarget = target;
        }
    }

    public void clearAllTargets() {
        this.target = null;
        this.gatherTarget = null;
        this.mustRecalculateSteps = true;
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        // Moving overrides other things in case we're moving towards our target
        if (movementSpeed != 0 && movePoint != null) {
            moveTick(world);
        } else if (target != null) {
            attackTick(world);
        } else if (gatherTarget != null) {
            gatherTick(world);
        }
    }

    private void attackTick(World world) {
        double distance = this.distanceTo(target);
        if (distance > range + target.getSize()/2 + getSize()/2) {
            if (this.movePoint == null) {
                // Move to be within range
                setMovePointNoCancel(new Point.Double(target.getX(), target.getY()));
                moveTick(world);
            }
            return;
        }

        if (attackCounter < attackTime) {
            attackCounter++;
        } else {
            attackCounter = 0;
            performAttack(target);
        }
    }

    private void gatherTick(World world) {
        double distance = this.distanceTo(gatherTarget);
        double maxRange = gatherTarget.getSize()/2 + this.getSize()/2 + 0.75;
        if (distance > maxRange) {
            if (this.movePoint == null) {
                // Move to be within range
                setMovePointNoCancel(new Point.Double(gatherTarget.getX(), gatherTarget.getY()));
                moveTick(world);
            }
            return;
        }

        performGather(world, gatherTarget);
    }

    private void moveTick(World world) {
        // If we haven't done any path finding yet, do it now.
        if (this.mustRecalculateSteps) {
            mustRecalculateSteps = false;
            Unit u = this;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Point.Double endPoint = world.getNearestEmptyPoint(getSize(), movePoint.x, movePoint.y, 10);
                        if (endPoint == null) {
                            movePointSteps.set(new Point.Double[]{movePoint});
                            return;
                        }
                        Point.Double[] path = world.getPath(new Point.Double(getX(), getY()), endPoint, u, getSize() / 2);
                        if (path == null) {
                            // We have no path so just try and go straight there
                            movePointSteps.set(new Point.Double[]{endPoint});
                        } else {
                            movePointSteps.set(path);
                        }
                        currentMovePointStep = 0;
                    } catch (NullPointerException e) {
                        System.out.println();
                    }
                }
            }).start();
            return;
        } else if (this.movePointSteps.get() == null) {
            return;
        }

        if (this.currentMovePointStep >= this.movePointSteps.get().length) {
            this.mustRecalculateSteps = true;
            return;
        }

        Point.Double movePoint = this.movePointSteps.get()[this.currentMovePointStep];

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
            this.mustRecalculateSteps = true; // Recalculate the path next time
        }
    }

    private void performGather(World world, Resource gatherTarget) {
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
