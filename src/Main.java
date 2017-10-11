import model.Board;
import model.Client;
import model.Tile;
import model.World;
import model.entity.TestUnit;
import model.entity.Unit;
import model.tile.GrassTile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Robert Campbell on 10/10/2017.
 */
public class Main {
    public static final int TICK_TIME_MS = 1000;

    public static void main(String[] args) {
        Board board = new Board(new Tile[][]{
                new Tile[]{new GrassTile(), new GrassTile()},
                new Tile[]{new GrassTile(), new GrassTile()}
        });
        World world = new World(board);
        Unit unit = new TestUnit(0.5, 0.5, 1, 0);
        world.addEntity(unit);
        unit.setMovePoint(new Point.Double(1.5, 1.5));

        Client client = new Client(World.NULL_WORLD);
        Server server = new Server(world);
        server.addClient(client);
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
