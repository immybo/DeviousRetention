package view;

import controller.MoveAction;
import model.Entity;
import model.World;
import model.entity.Unit;
import network.CTSConnection;
import network.STCConnection;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Client {
    private static final double KEY_MOVEMENT_SPEED = 1;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10;
    private static final double ZOOM_CHANGE_MULTIPLIER = 1.2;

    private JFrame frame;
    private JPanel panel;
    private World world;
    private CTSConnection server = null;

    private java.util.List<Integer> selectedIds;

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
        this.selectedIds = new ArrayList<Integer>();

        frame = new JFrame();
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                CoordinateTranslation translation = getCoordinateTranslation();
                getWorld().renderOn(g, translation);

                // Draw a box around all of the selected entities
                g.setColor(Color.BLACK);
                for (Integer i : getSelected()) {
                    Entity e = getWorld().getEntityByID(i);
                    Rectangle.Double bounds = e.getBounds();
                    Rectangle screenBounds = translation.toScreenCoordinates(bounds);
                    g.drawRect(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height);
                }
            }
        };

        frame.addMouseListener(new ClientMouseListener());
        frame.addKeyListener(new ClientKeyListener());
        frame.addMouseWheelListener(new ClientMouseWheelListener());

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

    private java.util.List<Integer> getSelected() {
        return selectedIds;
    }

    private CoordinateTranslation getCoordinateTranslation() {
        return new CoordinateTranslation((int)(xOffset*tileSize*zoom), (int)(yOffset*tileSize*zoom), tileSize*zoom, tileSize*zoom);
    }

    private class ClientMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point.Double pt = getCoordinateTranslation().toWorldCoordinates(new Point(e.getX(), e.getY()));
            if (e.getButton() == MouseEvent.BUTTON1) {
                Entity underMouse = getWorld().getEntityAt(pt);
                if (underMouse != null) {
                    selectedIds.clear();
                    selectedIds.add(underMouse.id);
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                if (server == null) {
                    System.err.println("Unable to send movement action as client is not connected.");
                } else {
                    for (Integer id : selectedIds) {
                        server.send(new MoveAction(id, pt));
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    private class ClientKeyListener implements KeyListener {
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
    }

    private class ClientMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int rotAmount = e.getWheelRotation();
            zoom *= Math.pow(ZOOM_CHANGE_MULTIPLIER, -rotAmount);
            zoom = zoom < MIN_ZOOM ? MIN_ZOOM : zoom > MAX_ZOOM ? MAX_ZOOM : zoom;
        }
    }
}
