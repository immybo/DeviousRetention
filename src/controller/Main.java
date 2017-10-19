package controller;

import model.Board;
import view.Client;
import model.Tile;
import model.World;
import model.entity.TestUnit;
import model.entity.Unit;
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

    public static void main(String[] args) {
        Board board = new Board(new Tile[][]{
                new Tile[]{new GrassTile(), new GrassTile()},
                new Tile[]{new GrassTile(), new GrassTile()}
        });
        World world = new World(board);
        Unit unit = new TestUnit(0.5, 0.5, 1, 0);
        world.addEntity(unit);
        unit.setMovePoint(new Point.Double(1.5, 1.5));

        Server server = new Server(world);
        STCConnection stc = new STCConnection(server);
        server.addClient(stc);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Client client = new Client(World.NULL_WORLD);
        CTSConnection cts = new CTSConnection(client);
        client.setServer(cts);

        server.updateClients();

        Timer timer = new Timer(TICK_TIME_MS, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                world.tick();
                server.updateClients();
            }
        });
        timer.start();
    }
}
