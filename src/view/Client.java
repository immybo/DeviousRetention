package view;

import controller.AttackAction;
import controller.MoveAction;
import model.Entity;
import model.Player;
import model.World;
import model.entity.Building;
import model.entity.OwnedEntity;
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
    private Player player;

    private java.util.List<Integer> selectedIds;

    private double xOffset;
    private double yOffset;
    private int tileSize;
    private double zoom;

    public Client(World world, Player player) {
        this.world = world;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zoom = 1;
        this.tileSize = 200;
        this.selectedIds = new ArrayList<Integer>();
        this.player = player;

        frame = new JFrame();
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D)g;
                CoordinateTranslation translation = getCoordinateTranslation();
                getWorld().renderOn(g2d, translation);

                // Draw a box around all of the selected entities
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(5));
                for (Integer i : getSelected()) {
                    Entity e = getWorld().getEntityByID(i);
                    if (e instanceof OwnedEntity) {
                        OwnedEntity ownedE = (OwnedEntity)e;
                        g2d.setColor(Player.getPlayerColor(ownedE.getPlayerNumber()));
                        Rectangle.Double bounds = e.getBounds();
                        Rectangle screenBounds = translation.toScreenCoordinates(bounds);
                        g2d.drawRect(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height);
                    }
                }
                g2d.setStroke(oldStroke);
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
                    boolean canSelect = !(underMouse instanceof OwnedEntity) || ((OwnedEntity)underMouse).getPlayerNumber() == player.getPlayerNumber();
                    if (canSelect) {
                        selectedIds.clear();
                        selectedIds.add(underMouse.id);
                    }
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                if (server == null) {
                    System.err.println("Unable to send movement action as client is not connected.");
                } else {
                    Entity entAt = getWorld().getEntityAt(pt);
                    for (Integer id : selectedIds) {
                        Entity selected = getWorld().getEntityByID(id);
                        if (entAt != null && selected instanceof Unit && ((Unit)selected).canAttack(entAt)) {
                            server.send(new AttackAction(id, entAt.id));
                        } else {
                            server.send(new MoveAction(id, pt));
                        }
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
