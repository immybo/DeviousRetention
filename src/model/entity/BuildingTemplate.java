package model.entity;

import controller.Cost;
import model.Entity;
import model.World;

import java.io.Serializable;

public class BuildingTemplate implements EntityTemplate, Serializable{
    private Entity.Ability[] abilities;

    private double size;
    private String name;

    public double range;
    public double attackTime;
    public int damage;
    public int maxHealth;
    public Cost cost;
    public String[] trainableUnits;
    public int[] trainTick;

    public BuildingTemplate(String name, Entity.Ability[] abilities, double range, double attackTime, int damage, double size, int maxHealth, Cost cost, String[] trainableUnits, int[] trainTick) {
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
    public Building create(World world, int playerNumber, double x, double y) {
        return new Building(world, x, y, size, playerNumber, maxHealth, trainableUnits, trainTick, name);
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
