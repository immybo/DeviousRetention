package model.entity;

/**
 * An entity is an immovable owned entity. They can train units, or research
 * new technologies.
 */
public abstract class Building extends OwnedEntity {
    public Building(double x, double y, double size, int playerNumber, int maxHealth) {
        super(x, y, size, playerNumber, maxHealth);
    }
}
