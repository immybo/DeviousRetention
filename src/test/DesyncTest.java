package test;

import junit.framework.Assert;
import network.CTSConnection;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import model.*;
import model.entity.*;
import model.tile.*;
import controller.*;
import view.Client;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DesyncTest {
    @Test
    public void testDesyncRecovery() {
        Tile[][] tiles = new Tile[30][30];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                tiles[i][j] = new GrassTile();
            }
        }

        for (int i = 12; i < 19; i++) {
            for (int j = 12; j < 19; j++) {
                tiles[i][j] = new MountainTile();
            }
        }

        Board board = new Board(tiles);

        UnitTemplate gatherer = new UnitTemplate("Gatherer", new Entity.Ability[]{Entity.Ability.ATTACK, Entity.Ability.GATHER}, 0.2, 1, 10, 10, 0.5, 100, new Cost(150), "gatherer.png");
        UnitTemplate archer = new UnitTemplate("Archer", new Entity.Ability[]{Entity.Ability.ATTACK}, 1, 6, 5, 20, 0.5, 100, new Cost(200), "archer.png");
        UnitTemplate infantry = new UnitTemplate("Infantry", new Entity.Ability[]{Entity.Ability.ATTACK}, 0.3, 1, 10, 75, 0.7, 200, new Cost(200), "infantry.png");
        UnitTemplate cavalry = new UnitTemplate("Cavalry", new Entity.Ability[]{Entity.Ability.ATTACK}, 1.5, 1, 10, 75, 1.2, 350, new Cost(600), "cavalry.png");

        BuildingTemplate headquarters = new BuildingTemplate("Headquarters", new Entity.Ability[]{}, 0, 1, 0, 1.5, 5000, new Cost(5000), new String[]{"Gatherer"}, new int[]{10});
        BuildingTemplate barracks = new BuildingTemplate("Barracks", new Entity.Ability[]{}, 0, 1, 0, 1, 2500, new Cost(2000), new String[]{"Archer", "Infantry", "Cavalry"}, new int[]{20, 20, 50});

        ResourceTemplate bigResource = new ResourceTemplate("Large Gold Mine", 2, 10000, 1);
        ResourceTemplate smallResource = new ResourceTemplate("Small Gold Mine", 1, 1500, 1.5);

        EntityTemplate[] allTemplates = new EntityTemplate[] {
                gatherer, archer, infantry, cavalry, headquarters, barracks, bigResource, smallResource
        };

        String[] defaultTemplates = new String[]{
                "Gatherer", "Headquarters"
        };

        World world = new World(board, allTemplates, defaultTemplates);

        int bottom = world.getBoard().getHeight();
        int right = world.getBoard().getWidth();

        world.addEntity(headquarters.create(world, 0, 4, 4));
        world.addEntity(gatherer.create(world, 0, 4, 6));
        world.addEntity(gatherer.create(world, 0, 4, 7));

        world.addEntity(headquarters.create(world, 1, right - 4, bottom - 4));
        world.addEntity(gatherer.create(world, 1, right - 4, bottom - 6));
        world.addEntity(gatherer.create(world, 1, right - 4, bottom - 7));

        world.addEntity(bigResource.create(world, -1, 10, 10));
        world.addEntity(bigResource.create(world, -1, 20, 20));
        world.addEntity(smallResource.create(world, -1, 20, 10));
        world.addEntity(smallResource.create(world, -1, 10, 20));

        Server server = new Server(world);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launchClient(0);
        launchClient(1);

        server.updateClients();
        world.tick();

        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(()->{server.tick();}, 0, 16, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        // Redirect system.out and system.err so we can read from any errors. Desync doesn't throw an exception
        // because it should be recovering from it.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        System.setErr(new PrintStream(err));
        System.setOut(new PrintStream(out));

        // Now desync the server
        server.getWorld().addEntity(gatherer.create(server.getWorld(), 1, 5, 5));

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().equals("pool-1-thread-1")) {
                try {
                    t.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Wait for a little bit; during this time we should get an error
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        String[] outStr = out.toString().split("\n");
        out.reset();
        Assert.assertTrue(outStr.length > 1);

        // Now we wait, and the desync should be resolved by now so there should be nothing printed out
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        outStr = out.toString().split("\n");

        System.out.println(outStr);

        Assert.assertTrue(outStr.length == 1);
    }

    private static void launchClient(int number) {
        Player player = new Player(number);
        player.earnCredits(10000);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Client client = new Client(World.NULL_WORLD, player.getPlayerNumber());
                CTSConnection cts = new CTSConnection(client, player);
                client.setServer(cts);
            }
        });
    }
}
