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
public class Building extends OwnedEntity {
    private final UnitTemplate[] trainableUnits;
    private final int[] trainTicks;

    private int currentTrainTick;
    private Queue<UnitTemplate> trainQueue;

    public Building(double x, double y, double size, int playerNumber, int maxHealth, UnitTemplate[] trainableUnits, int[] trainTicks) {
        super(x, y, size, playerNumber, maxHealth);

        this.trainableUnits = trainableUnits;
        this.trainTicks = trainTicks;

        currentTrainTick = 0;
        trainQueue = new LinkedList<UnitTemplate>();
    }

    public UnitTemplate[] trainableUnits() {
        return trainableUnits;
    }

    public boolean canTrain(UnitTemplate unit) {
        for (UnitTemplate u : trainableUnits()) {
            if (u.getName().equals(unit.getName())) {
                return true;
            }
        }
        return false;
    }

    private int ticksToTrain(UnitTemplate unit) {
        for (int i = 0; i < trainableUnits.length; i++) {
            if (trainableUnits[i].getName().equals(unit.getName())) {
                return trainTicks[i];
            }
        }
        throw new IllegalArgumentException("Building " + getClass().getCanonicalName() + " can't train unit " + unit.getName());
    }

    public void train(UnitTemplate unit) {
        if (!canTrain(unit)) {
            throw new IllegalArgumentException("Building " + getClass().getCanonicalName() + " can't train unit " + unit.getName());
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
                UnitTemplate unitType = trainQueue.poll();
                Point.Double spawnPoint = getUnitSpawnCoordinates();
                world.addEntity(unitType.create(world, this.getPlayerNumber(), spawnPoint.x, spawnPoint.y));
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
            UnitTemplate u = trainableUnits[i];
            actions[i] = new TrainAction(id, u);
        }
        return actions;
    }
}
