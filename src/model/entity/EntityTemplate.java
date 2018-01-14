package model.entity;

import model.Entity;
import model.World;

/**
 * Any entity template can be instantiated to get an entity.
 * Generally, a set of these should be generated at the start of the game
 * and passed to the relevant classes (e.g. UnitTemplates can be passed to
 * buildings to allow training of them).
 */
public interface EntityTemplate {
    public Entity create(World world, int playerNumber, double x, double y);
    public int getCost();
    public String getName();
}
