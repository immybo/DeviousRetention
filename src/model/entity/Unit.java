package model.entity;

/**
 * An entity which is owned by a player, which can be moved around and can perform actions
 * such as attacking or healing.
 */
public abstract class Unit extends OwnedEntity {
    public Unit(double x, double y, double size, int playerNumber) {
        super(x, y, size, playerNumber);
    }
}
