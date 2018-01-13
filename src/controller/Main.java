package controller;

import model.*;
import model.entity.*;
import model.tile.MountainTile;
import view.Client;
import model.tile.GrassTile;
import network.CTSConnection;
import network.STCConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Robert Campbell on 10/10/2017.
 */
public class Main {
    public static final int TICK_TIME_MS = 50;

    private static int currentPlayerNumber;

    public static void main(String[] args) {
        currentPlayerNumber = 0;

        EntityManager.initialise();

        Tile[][] tiles = new Tile[10][10];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                tiles[i][j] = new GrassTile();
            }
        }
        tiles[5][5] = new MountainTile();
        Board board = new Board(tiles);

        UnitTemplate testUnit = new UnitTemplate(new Entity.Ability[]{Entity.Ability.ATTACK, Entity.Ability.GATHER}, 1, 2, 10, 50, 1, 100);

        World world = new World(board);
        Building building = new TestBuilding(3, 3, 0);
        Resource resource = new Resource(4.5, 4.5, 0.3, 500, 2);
        world.addEntity(testUnit.create(world, 0, 0.5, 0.5));
        world.addEntity(testUnit.create(world, 1, 1.5, 1.5));
        world.addEntity(building);
        world.addEntity(resource);

        Server server = new Server(world);
        launchClient(server);
        launchClient(server);

        server.updateClients();
        world.tick();

        Timer timer = new Timer(TICK_TIME_MS, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                world.tick();
                server.updateClients();
            }
        });
        timer.start();
    }

    private static void launchClient(Server server) {
        Player player = new Player(currentPlayerNumber++);
        player.earnCredits(10000);
        STCConnection stc = new STCConnection(server);
        server.addPlayer(player);
        server.addClient(stc);
        Client client = new Client(World.NULL_WORLD, player.getPlayerNumber());
        CTSConnection cts = new CTSConnection(client);
        client.setServer(cts);
    }
}
