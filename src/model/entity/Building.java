package model.entity;

import controller.Action;
import controller.TrainAction;
import model.World;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * An entity is an immovable owned entity. They can train units, or research
 * new technologies.
 */
public abstract class Building extends OwnedEntity {
    private final EntityManager.UNIT[] trainableUnits;
    private final int[] trainTicks;

    private int currentTrainTick;
    private Queue<EntityManager.UNIT> trainQueue;

    public Building(double x, double y, double size, int playerNumber, int maxHealth, EntityManager.UNIT[] trainableUnits, int[] trainTicks) {
        super(x, y, size, playerNumber, maxHealth);

        this.trainableUnits = trainableUnits;
        this.trainTicks = trainTicks;

        currentTrainTick = 0;
        trainQueue = new LinkedList<EntityManager.UNIT>();
    }

    public EntityManager.UNIT[] trainableUnits() {
        return trainableUnits;
    }

    public boolean canTrain(EntityManager.UNIT unit) {
        for (EntityManager.UNIT u : trainableUnits()) {
            if (u == unit) {
                return true;
            }
        }
        return false;
    }

    private int ticksToTrain(EntityManager.UNIT unit) {
        for (int i = 0; i < trainableUnits.length; i++) {
            if (trainableUnits[i] == unit) {
                return trainTicks[i];
            }
        }
        throw new IllegalArgumentException("Building " + getClass().getCanonicalName() + " can't train unit " + unit.name());
    }

    public void train(EntityManager.UNIT unit) {
        if (!canTrain(unit)) {
            throw new IllegalArgumentException("Building " + getClass().getCanonicalName() + " can't train unit " + unit.name());
        }

        trainQueue.add(unit);
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        if (!trainQueue.isEmpty()) {
            currentTrainTick++;
            int finalTick = ticksToTrain(trainQueue.peek());
            if (currentTrainTick >= finalTick) {
                EntityManager.UNIT unitType = trainQueue.poll();
                Point.Double spawnPoint = getUnitSpawnCoordinates();
                world.addEntity(EntityManager.instantiate(unitType, spawnPoint.x, spawnPoint.y, this.getPlayerNumber()));
                currentTrainTick = 0;
            }
        }
    }

    private Point.Double getUnitSpawnCoordinates() {
        return new Point.Double(this.getX(), this.getY());
    }

    @Override
    public Action[] getActions() {
        Action[] actions = new Action[trainableUnits.length];
        for (int i = 0; i < actions.length; i++) {
            EntityManager.UNIT u = trainableUnits[i];
            actions[i] = new TrainAction(id, u);
        }
        return actions;
    }
}
