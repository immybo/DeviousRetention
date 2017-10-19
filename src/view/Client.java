package view;

import controller.MoveAction;
import model.World;
import model.entity.Unit;
import network.CTSConnection;
import network.STCConnection;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private static final double KEY_MOVEMENT_SPEED = 1;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10;
    private static final double ZOOM_CHANGE_MULTIPLIER = 1.2;

    private JFrame frame;
    private JPanel panel;
    private World world;
    private CTSConnection server = null;

    private double xOffset;
    private double yOffset;
    private int tileSize;
    private double zoom;

    public Client(World world) {
        this.world = world;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zoom = 1;
        this.tileSize = 200;

        frame = new JFrame();
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                int worldWidth = getWorld().getBoard().getWidth();
                int worldHeight = getWorld().getBoard().getHeight();
                getWorld().renderOn(g, getCoordinateTranslation());
            }
        };

        frame.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Point.Double pt = getCoordinateTranslation().toWorldCoordinates(new Point(e.getX(), e.getY()));
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

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    xOffset += KEY_MOVEMENT_SPEED;
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    xOffset -= KEY_MOVEMENT_SPEED;
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    yOffset += KEY_MOVEMENT_SPEED;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    yOffset -= KEY_MOVEMENT_SPEED;
                }
            }
        });

        frame.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int rotAmount = e.getWheelRotation();
                zoom *= Math.pow(ZOOM_CHANGE_MULTIPLIER, -rotAmount);
                zoom = zoom < MIN_ZOOM ? MIN_ZOOM : zoom > MAX_ZOOM ? MAX_ZOOM : zoom;
            }
        });

        frame.add(panel);
        frame.setMinimumSize(new Dimension(1920, 1080));
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

    private CoordinateTranslation getCoordinateTranslation() {
        return new CoordinateTranslation((int)(xOffset*tileSize*zoom), (int)(yOffset*tileSize*zoom), tileSize*zoom, tileSize*zoom);
    }
}
