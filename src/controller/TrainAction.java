package controller;

import model.World;
import model.entity.Building;
import model.entity.EntityManager;

/**
 * Created by Robert Campbell on 28/10/2017.
 */
public class TrainAction implements Action {
    private final int buildingId;
    private final EntityManager.UNIT unitType;

    public TrainAction(int buildingId, EntityManager.UNIT unitType) {
        this.buildingId = buildingId;
        this.unitType = unitType;
    }

    @Override
    public void run(World world) {
        ((Building)world.getEntityByID(buildingId)).train(unitType);
    }
}
