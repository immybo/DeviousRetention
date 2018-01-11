package controller;

import model.Player;
import model.World;
import model.entity.Building;
import model.entity.EntityManager;

/**
 * Created by Robert Campbell on 28/10/2017.
 */
public class TrainAction extends Action {
    private final int buildingId;
    public final EntityManager.UNIT unitType;

    public TrainAction(int buildingId, EntityManager.UNIT unitType) {
        this.buildingId = buildingId;
        this.unitType = unitType;
    }

    @Override
    public void run(World world) {
        Building trainer = (Building)world.getEntityByID(buildingId);
        Player player = world.getPlayer(trainer.getPlayerNumber());
        if (player.spendCredits(EntityManager.getUnitCost(unitType))) {
            ((Building)world.getEntityByID(buildingId)).train(unitType);
        } // Else, failure. Do nothing.
    }
}
