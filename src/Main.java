import model.Board;
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

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                world.renderOn(g);
            }
        };

        frame.add(panel);
        frame.setMinimumSize(new Dimension(500, 500));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.repaint();
    }
}
