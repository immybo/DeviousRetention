package model.entity;

import controller.Cost;
import model.Entity;
import model.World;

import java.io.Serializable;

public class ResourceTemplate implements EntityTemplate, Serializable {
    private double size;
    private int totalAmount;
    private double efficiency;
    private String name;

    public ResourceTemplate(String name, double size, int totalAmount, double efficiency) {
        this.name = name;
        this.size = size;
        this.totalAmount = totalAmount;
        this.efficiency = efficiency;
    }

    @Override
    public Resource create(World world, int playerNumber, double x, double y) {
        return new Resource(world, x, y, size, totalAmount, efficiency, name);
    }

    @Override
    public Cost getCost() {
        return Cost.NULL_COST;
    }

    @Override
    public String getName() {
        return name;
    }
}
