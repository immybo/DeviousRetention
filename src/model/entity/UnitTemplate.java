package model.entity;

import controller.Cost;
import model.Entity;
import model.World;

import java.awt.*;
import java.io.Serializable;

/**
 * Defines a template for a unit, which can be created.
 */
public class UnitTemplate implements EntityTemplate, Serializable {

    private Entity.Ability[] abilities;

    private double movementSpeed;
    private double range;
    private double attackTime;
    private int damage;
    private double size;
    private int maxHealth;
    private String name;
    private Cost cost;
    private String imageName;

    public UnitTemplate(String name, Entity.Ability[] abilities, double movementSpeed, double range, double attackTime, int damage, double size, int maxHealth, Cost cost, String imageName) {
        this.name = name;
        this.abilities = abilities;
        this.movementSpeed = movementSpeed;
        this.range = range;
        this.attackTime = attackTime;
        this.damage = damage;
        this.size = size;
        this.maxHealth = maxHealth;
        this.cost = cost;
        this.imageName = imageName;
    }

    @Override
    public Unit create(World world, int playerNumber, double x, double y) {
        return new Unit(x, y, size, range, damage, maxHealth, playerNumber, movementSpeed, abilities, imageName, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Cost getCost() {
        return cost;
    }

    public double getSize() {
        return size;
    }
}
