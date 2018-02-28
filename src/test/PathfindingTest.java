package test;

import controller.Cost;
import controller.MoveAction;
import model.entity.*;
import model.tile.GrassTile;
import model.tile.MountainTile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import model.*;

import java.awt.*;

public class PathfindingTest {
    private static Board board;

    private static UnitTemplate testUnit;
    private static BuildingTemplate testBuilding;

    private static EntityTemplate[] allTemplates;
    private static String[] defaultTemplates;

    private World world;

    @BeforeClass
    public static void classSetUp() {
        Tile[][] tiles = new Tile[10][10];
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = new GrassTile();
            }
        }

        tiles[4][4] = new MountainTile();
        tiles[4][5] = new MountainTile();
        tiles[5][4] = new MountainTile();
        tiles[5][5] = new MountainTile();

        board = new Board(tiles);

        testUnit = new UnitTemplate("TestUnit", new Entity.Ability[]{Entity.Ability.ATTACK, Entity.Ability.GATHER}, 1, 1, 1, 1, 1, 1, new Cost(1), null);
        testBuilding = new BuildingTemplate("TestBuilding", new Entity.Ability[]{Entity.Ability.ATTACK}, 5, 1, 1, 3, 1, new Cost(1), new String[]{"TestUnit"}, new int[]{1});

        allTemplates = new EntityTemplate[]{testUnit, testBuilding};
        defaultTemplates = new String[]{"TestUnit", "TestBuilding"};
    }

    @Before
    public void setUp() {
        world = new World(board, allTemplates, defaultTemplates);
        EntityManager em = world.getEntityManager();
    }

    @Test
    public void noObstacles() {
        Unit u = testUnit.create(world, 0, 1, 1);
        world.addEntity(u);
        world.tick();
        new MoveAction(u.id, new Point.Double(1, 4)).run(world);

        world.tick();
        TestUtil.sleep(200);
        TestUtil.do1000Ticks(world);

        double distanceToTarget = new Point.Double(u.getX(), u.getY()).distance(1, 4);
        Assert.assertTrue(distanceToTarget <= World.PATHFINDING_GRANULARITY);
    }

    @Test
    public void terrainObstacle() {
        Unit u = testUnit.create(world, 0, 1, 1);
        world.addEntity(u);
        world.tick();
        new MoveAction(u.id, new Point.Double(8, 8)).run(world);

        world.tick();
        TestUtil.sleep(200);
        TestUtil.do1000Ticks(world);

        double distanceToTarget = new Point.Double(u.getX(), u.getY()).distance(8, 8);
        Assert.assertTrue(distanceToTarget <= World.PATHFINDING_GRANULARITY);
    }

    @Test
    public void unitObstacle() {
        Unit u = testUnit.create(world, 0, 1, 1);
        world.addEntity(u);
        Unit u2 = testUnit.create(world, 0, 1, 3);
        world.addEntity(u2);
        world.tick();
        new MoveAction(u.id, new Point.Double(1, 5)).run(world);

        world.tick();
        TestUtil.sleep(200);
        TestUtil.do1000Ticks(world);

        double distanceToTarget = new Point.Double(u.getX(), u.getY()).distance(1, 5);
        Assert.assertTrue(distanceToTarget <= World.PATHFINDING_GRANULARITY);
    }

    @Test
    public void buildingObstacle() {
        Unit u = testUnit.create(world, 0, 1, 1);
        world.addEntity(u);
        Building b = testBuilding.create(world, 0, 1.5, 3.5);
        world.addEntity(b);
        world.tick();
        new MoveAction(u.id, new Point.Double(1, 7)).run(world);

        world.tick();
        TestUtil.sleep(200);
        TestUtil.do1000Ticks(world);

        double distanceToTarget = new Point.Double(u.getX(), u.getY()).distance(1, 7);
        Assert.assertTrue(distanceToTarget <= World.PATHFINDING_GRANULARITY);
    }

    @Test
    public void moveOntoBuilding() {
        Unit u = testUnit.create(world, 0, 1, 1);
        world.addEntity(u);
        Building b = testBuilding.create(world, 0, 3, 3);
        world.addEntity(b);
        world.tick();
        new MoveAction(u.id, new Point.Double(3, 3)).run(world);

        world.tick();
        TestUtil.sleep(200);
        TestUtil.do1000Ticks(world);

        double distanceToTarget = new Point.Double(u.getX(), u.getY()).distance(3, 3);
        Assert.assertTrue(distanceToTarget <= World.PATHFINDING_GRANULARITY + b.getSize()/2 + u.getSize()/2 + 0.5);
    }
}
