package model;

import controller.MoveAction;
import model.entity.Unit;
import network.CTSConnection;
import network.STCConnection;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Client {
    private JFrame frame;
    private World world;
    private CTSConnection server = null;

    public Client(World world) {
        this.world = world;

        frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                int worldWidth = getWorld().getBoard().getWidth();
                int worldHeight = getWorld().getBoard().getHeight();
                getWorld().renderOn(g, new CoordinateTranslation(0, 0, getWidth()/worldWidth, getHeight()/worldHeight));
            }
        };

        panel.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int worldWidth = getWorld().getBoard().getWidth();
                    int worldHeight = getWorld().getBoard().getHeight();
                    CoordinateTranslation translate = new CoordinateTranslation(0, 0, panel.getWidth()/worldWidth, panel.getHeight()/worldHeight);
                    Point.Double pt = translate.toWorldCoordinates(new Point(e.getX(), e.getY()));
                    if (server == null) {
                        System.err.println("Unable to send movement action as client is not connected.");
                    } else {
                        server.send(new MoveAction(getWorld().getEntities()[0].id, pt));
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        frame.add(panel);
        frame.setMinimumSize(new Dimension(500, 500));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.repaint();
    }

    public void setServer(CTSConnection server) {
        this.server = server;
    }

    public void setWorld(World world) {
        this.world = world;
        frame.repaint();
    }

    private World getWorld() {
        return world;
    }
}
