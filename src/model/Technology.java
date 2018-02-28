package model;

import model.entity.EntityTemplate;

import java.io.Serializable;
import java.util.Map;

public abstract class Technology implements Serializable {
    /**
     * A technology will generally unlock some types of entity for creation by the player,
     * and change some existing types of entities. For example, a technology could unlock
     * the creation of TestUnits and add 20 maximum health to TestBuildings. A technology
     * can change anything else in the world, player, or list of templates.
     *
     * The three methods in this interface will be applied, in the below order, when this
     * technology is researched by the player.
     */

    private final int playerNumber;

    public Technology(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public abstract String[] getEntityUnlocks();
    public abstract void applyToTemplates(Map<String, EntityTemplate> templates);
    public abstract void apply(Player p, World w);

    public int getPlayerNumber() {
        return playerNumber;
    }
}
