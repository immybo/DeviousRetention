package controller;

import model.Board;
import model.Player;
import model.entity.*;
import model.tile.MountainTile;
import view.Client;
import model.Tile;
import model.World;
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

        World world = new World(board);
        Unit unit = new TestUnit(0.5, 0.5, 0);
        Unit unit2 = new TestUnit(1.5, 0.5, 1);
        Building building = new TestBuilding(3, 3, 0);
        world.addEntity(unit);
        world.addEntity(unit2);
        world.addEntity(building);

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
        server.addClient(stc);
        Client client = new Client(World.NULL_WORLD, player);
        CTSConnection cts = new CTSConnection(client);
        client.setServer(cts);
    }
}
