package model.entity;

import model.Entity;

public class Resource extends Entity {
    private final int totalAmount;
    private int currentAmount;
    private final double efficiency;

    public Resource(double x, double y, double size, int totalAmount, double efficiency) {
        super(x, y, size);

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
}
