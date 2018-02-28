package model.technology;

import controller.Cost;
import model.Entity;
import model.Player;
import model.Technology;
import model.World;
import model.entity.BuildingTemplate;
import model.entity.EntityTemplate;
import model.entity.UnitTemplate;

import java.util.Map;

public class TestTechnology extends Technology {
    public TestTechnology(int playerNumber) {
        super(playerNumber);
    }

    @Override
    public String[] getEntityUnlocks() {
        return new String[]{};
    }

    @Override
    public void applyToTemplates(Map<String, EntityTemplate> templates) {
        ((UnitTemplate)templates.get("Gatherer")).movementSpeed += 2;
    }

    @Override
    public void apply(Player p, World w) {

    }
}
