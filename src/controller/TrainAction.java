package controller;

import model.Player;
import model.World;
import model.entity.Building;
import model.entity.UnitTemplate;

/**
 * Created by Robert Campbell on 28/10/2017.
 */
public class TrainAction extends Action {
    private final int buildingId;
    public final String unitType;

    public TrainAction(int buildingId, String unitType) {
        this.buildingId = buildingId;
        this.unitType = unitType;
    }

    @Override
    public void run(World world) {
        Building trainer = (Building)world.getEntityByID(buildingId);
        Player player = world.getPlayer(trainer.getPlayerNumber());

        UnitTemplate unitTypeTemplate = (UnitTemplate)world.getEntityManager().getEntityTemplateByName(unitType);

        if (player.spendCredits(unitTypeTemplate.getCost())) {
            ((Building)world.getEntityByID(buildingId)).train(unitTypeTemplate);
        } // Else, failure. Do nothing.
    }
}
