package model.entity;

import model.Entity;

public abstract class OwnedEntity extends Entity {
    private int playerNumber;

    public OwnedEntity(double x, double y, double size, int playerNumber) {
        super(x, y, size);
        this.playerNumber = playerNumber;
    }

    public int getPlayer() {
        return playerNumber;
    }
}
