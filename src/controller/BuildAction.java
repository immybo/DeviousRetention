package controller;

import model.Player;
import model.World;
import model.entity.BuildingTemplate;

/**
 * Created by Robert Campbell on 20/01/2018.
 */
public class BuildAction extends Action {
    private final double x;
    private final double y;
    public final BuildingTemplate buildingType;
    private final Cost cost;
    private final int playerNumber;

    public BuildAction(BuildingTemplate buildingType, int playerNumber, double x, double y) {
        this.x = x;
        this.y = y;
        this.buildingType = buildingType;
        this.cost = buildingType.getCost();
        this.playerNumber = playerNumber;
    }

    @Override
    public void run(World world) {
        // TODO collision checking.
        Player player = world.getPlayer(playerNumber);
        if (player.spendCredits(buildingType.getCost())) {
            world.addEntity(buildingType.create(world, playerNumber, x, y));
        }
    }

    @Override
    public Cost getCost() {
        return this.cost;
    }
}
