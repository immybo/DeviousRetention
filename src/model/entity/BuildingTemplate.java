package model.entity;

import controller.Cost;
import model.Entity;
import model.World;

import java.io.Serializable;

public class BuildingTemplate implements EntityTemplate, Serializable{
    private Entity.Ability[] abilities;

    private double range;
    private double attackTime;
    private int damage;
    private double size;
    private int maxHealth;
    private String name;
    private Cost cost;
    private UnitTemplate[] trainableUnits;
    private int[] trainTick;

    public BuildingTemplate(String name, Entity.Ability[] abilities, double range, double attackTime, int damage, double size, int maxHealth, Cost cost, UnitTemplate[] trainableUnits, int[] trainTick) {
        this.name = name;
        this.abilities = abilities;
        this.range = range;
        this.attackTime = attackTime;
        this.damage = damage;
        this.size = size;
        this.maxHealth = maxHealth;
        this.cost = cost;
        this.trainableUnits = trainableUnits;
        this.trainTick = trainTick;
    }

    @Override
    public Entity create(World world, int playerNumber, double x, double y) {
        return new Building(x, y, size, playerNumber, maxHealth, trainableUnits, trainTick);
    }

    @Override
    public Cost getCost() {
        return cost;
    }

    @Override
    public String getName() {
        return name;
    }
}
