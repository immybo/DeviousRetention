package model.entity;

import model.Entity;
import model.World;

import java.awt.*;

public abstract class OwnedEntity extends Entity {
    public static final Color[] PLAYER_COLORS = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

    private int playerNumber;
    private int maxHealth;
    private int health;

    public OwnedEntity(World world, double x, double y, double size, int playerNumber, int maxHealth, String imageName, String name) {
        super(world, x, y, size, imageName, name);
        this.playerNumber = playerNumber;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public OwnedEntity(World world, double x, double y, double size, int playerNumber, int maxHealth, String name) {
        super(world, x, y, size, name);
        this.playerNumber = playerNumber;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void changeHealth(int difference) {
        this.health += difference;
    }

    public void setHealth(int newValue) {
        this.health = newValue;
    }

    public int getHealth() {
        return this.health;
    }

    public double getHealthProportion() {
        if (this.maxHealth == 0) {
            return 1;
        } else {
            return ((double)getHealth() / maxHealth);
        }
    }

    @Override
    public void tick(World world) {
        super.tick(world);

        if (getHealth() < 0) {
            world.removeEntity(this);
        }
    }

    @Override
    public int hashCode() {
        return playerNumber*7 + getHealth()*13 + super.hashCode();
    }
}
