package controller;

import model.Player;
import model.World;
import model.entity.Building;
import model.entity.EntityManager;
import model.entity.UnitTemplate;

/**
 * Created by Robert Campbell on 28/10/2017.
 */
public class TrainAction extends Action {
    private final int buildingId;
    public final UnitTemplate unitType;

    public TrainAction(int buildingId, UnitTemplate unitType) {
        this.buildingId = buildingId;
        this.unitType = unitType;
    }

    @Override
    public void run(World world) {
        Building trainer = (Building)world.getEntityByID(buildingId);
        Player player = world.getPlayer(trainer.getPlayerNumber());
        if (player.spendCredits(unitType.getCost())) {
            ((Building)world.getEntityByID(buildingId)).train(unitType);
        } // Else, failure. Do nothing.
    }
}
