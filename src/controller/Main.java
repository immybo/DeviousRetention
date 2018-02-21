package controller;

import model.*;
import model.entity.*;
import model.tile.MountainTile;
import view.Client;
import model.tile.GrassTile;
import network.CTSConnection;
import network.STCConnection;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Created by Robert Campbell on 10/10/2017.
 */
public class Main {
    public static final int TICK_TIME_MS = 16;

    private static int currentPlayerNumber;

    public static void main(String[] args) {
        currentPlayerNumber = 0;

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

        BuildingTemplate headquarters = new BuildingTemplate("Headquarters", new Entity.Ability[]{}, 0, 1, 0, 1.5, 5000, new Cost(5000), new UnitTemplate[]{gatherer}, new int[]{10});
        BuildingTemplate barracks = new BuildingTemplate("Barracks", new Entity.Ability[]{}, 0, 1, 0, 1, 2500, new Cost(2000), new UnitTemplate[]{archer, infantry, cavalry}, new int[]{20, 20, 50});

        ResourceTemplate bigResource = new ResourceTemplate("Large Gold Mine", 2, 10000, 1);
        ResourceTemplate smallResource = new ResourceTemplate("Small Gold Mine", 1, 1500, 1.5);

        World world = new World(board);

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

        launchClient(server, new BuildingTemplate[]{headquarters, barracks});
        launchClient(server, new BuildingTemplate[]{headquarters, barracks});

        server.updateClients();
        world.tick();

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                server.tick();
            }
        }, 0, TICK_TIME_MS);
    }

    private static void launchClient(Server server, BuildingTemplate[] buildable) {
        Player player = new Player(currentPlayerNumber++, buildable);
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
