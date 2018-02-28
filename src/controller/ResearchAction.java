package controller;

import model.Technology;
import model.World;

public class ResearchAction extends Action {
    private final Technology technology;

    public ResearchAction(Technology technology) {
        this.technology = technology;
    }

    @Override
    public void run(World world) {
        world.applyTechnology(technology);
    }
}
