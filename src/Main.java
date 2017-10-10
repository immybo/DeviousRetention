import model.Board;
import model.Client;
import model.Tile;
import model.World;
import model.tile.GrassTile;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Robert Campbell on 10/10/2017.
 */
public class Main {
    public static void main(String[] args) {
        Board board = new Board(new Tile[][]{
                new Tile[]{new GrassTile(), new GrassTile()},
                new Tile[]{new GrassTile(), new GrassTile()}
        });
        World world = new World(board);

        Client client = new Client(World.NULL_WORLD);
        Server server = new Server(world);
        server.addClient(client);
        server.updateClients();
    }
}
