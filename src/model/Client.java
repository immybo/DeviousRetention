package model;

import network.STCConnection;

import javax.swing.*;
import java.awt.*;

public class Client implements STCConnection {
    private JFrame frame;
    private World world;

    public Client(World world) {
        this.world = world;


        frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                getWorld().renderOn(g);
            }
        };

        frame.add(panel);
        frame.setMinimumSize(new Dimension(500, 500));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.repaint();
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
        frame.repaint();
    }

    private World getWorld() {
        return world;
    }
}
