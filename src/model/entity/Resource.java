package model.entity;

import model.Entity;
import model.World;

public class Resource extends Entity {
    private final int totalAmount;
    private int currentAmount;
    private final double efficiency;

    public Resource(World world, double x, double y, double size, int totalAmount, double efficiency, String name) {
        super(world, x, y, size, name);

        this.totalAmount = totalAmount;
        this.currentAmount = this.totalAmount;
        this.efficiency = efficiency;
    }

    public int getRemainingCredits() {
        return currentAmount;
    }

    public int getMaxCredits() {
        return totalAmount;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void takeCredits(int num) {
        currentAmount -= num;
    }
}
