package controller;

import model.Entity;
import model.World;
import model.entity.Resource;
import model.entity.Unit;

/**
 * Created by Robert Campbell on 30/12/2017.
 */
public class GatherAction extends Action {
    private int unitId;
    private int resourceId;

    public GatherAction(int unitId, int resourceId) {
        this.unitId = unitId;
        this.resourceId = resourceId;
    }

    @Override
    public void run(World world) {
        Entity eGatherer = world.getEntityByID(unitId);
        Entity eResource = world.getEntityByID(resourceId);

        if (eGatherer instanceof Unit && eResource instanceof Resource) {
            Unit gatherer = (Unit) eGatherer;
            Resource resource = (Resource) eResource;
            gatherer.setGatherTarget(resource);
        } else {
            throw new IllegalArgumentException("Gatherer not a unit or resource not a resource when trying to gather.");
        }
    }
}
