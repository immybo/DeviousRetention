package model.entity;

public class TestBuilding extends Building {
    public TestBuilding(double x, double y, int playerNumber) {
        super(x, y, 2, playerNumber, 100, new EntityManager.UNIT[]{EntityManager.UNIT.TEST_UNIT}, new int[]{20});
    }
}
