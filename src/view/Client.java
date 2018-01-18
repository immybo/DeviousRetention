package view;

import controller.*;
import controller.Action;
import model.Entity;
import model.Player;
import model.World;
import model.entity.*;
import network.CTSConnection;
import network.STCConnection;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private static final double KEY_MOVEMENT_SPEED = 1;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10;
    private static final double ZOOM_CHANGE_MULTIPLIER = 1.2;
    private static final Font CREDITS_FONT = new Font("Arial", Font.BOLD, 50);

    private JFrame frame;
    private JPanel panel;
    private JPanel infoPanel;
    private JPanel selectionPanel;
    private World world;
    private CTSConnection server = null;

    private java.util.List<Integer> selectedIds;

    private double xOffset;
    private double yOffset;
    private int tileSize;
    private double zoom;

    private int mousePressX = -1;
    private int mousePressY = -1;
    private int mousePressType = -1;

    private int mouseX = -1;
    private int mouseY = -1;

    private final int playerNumber;

    public Client(World world, int playerNumber) {
        this.world = world;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zoom = 1;
        this.tileSize = 200;
        this.selectedIds = new ArrayList<Integer>();
        this.playerNumber = playerNumber;

        frame = new JFrame();

        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
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

                // Draw a box around the selected area, if there is one
                if (mousePressX != -1) {
                    g2d.setColor(Color.BLACK);
                    int left = Math.min(mousePressX, mouseX);
                    int top = Math.min(mousePressY, mouseY);
                    int width = Math.max(mousePressX, mouseX) - left;
                    int height = Math.max(mousePressY, mouseY) - top;
                    g2d.drawRect(left, top, width, height);
                }
            }
        };

        infoPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;

                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

                int x = 20;
                int y = 100;

                g2d.setFont(Client.CREDITS_FONT);
                g2d.setColor(Color.BLACK);
                g2d.drawString("[empire name]", x, y);

                y += 100;

                // It can be null if we haven't received anything from the server yet.
                if (getWorld().getPlayer(playerNumber) != null)
                    g2d.drawString("Credits: " + getWorld().getPlayer(playerNumber).getNumCredits(), x, y);

                y += 200;
            }
        };

        selectionPanel = new JPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(500, 1080));
        rightPanel.setLayout(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(500, 800));
        rightPanel.add(infoPanel, BorderLayout.NORTH);
        rightPanel.add(selectionPanel, BorderLayout.SOUTH);

        frame.addMouseMotionListener(new ClientMouseMotionListener());
        frame.addMouseListener(new ClientMouseListener());
        frame.addKeyListener(new ClientKeyListener());
        frame.addMouseWheelListener(new ClientMouseWheelListener());

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setMinimumSize(new Dimension(1920, 1080));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.pack();
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

    private class ClientMouseMotionListener implements MouseMotionListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (mousePressX != -1) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    private class ClientMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePressX = e.getX();
            mousePressY = e.getY();
            mousePressType = e.getButton();
            mouseX = e.getX();
            mouseY = e.getY();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point.Double pt = getCoordinateTranslation().toWorldCoordinates(new Point(e.getX(), e.getY()));
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (mousePressX == -1 || mousePressType != MouseEvent.BUTTON1) {
                    Entity underMouse = getWorld().getEntityAt(pt);
                    if (underMouse != null) {
                        trySelect(new Entity[]{underMouse});
                    }
                } else {
                    Point.Double pressPt = getCoordinateTranslation().toWorldCoordinates(new Point(mousePressX, mousePressY));
                    double left = Math.min(pressPt.getX(), pt.getX());
                    double top = Math.min(pressPt.getY(), pt.getY());
                    // The +0.001 means that a click can be counted as a drag
                    double width = Math.max(pressPt.getX(), pt.getX()) - left + 0.001;
                    double height = Math.max(pressPt.getY(), pt.getY()) - top + 0.001;


                    Entity[] underMouse = getWorld().getEntitiesIn(new Rectangle.Double(left, top, width, height));
                    trySelect(underMouse);

                    mousePressX = -1;
                    mousePressY = -1;
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                if (server == null) {
                    System.err.println("Unable to send movement action as client is not connected.");
                } else {
                    Entity[] entsAt = getWorld().getEntitiesAt(pt);
                    for (Integer id : selectedIds) {
                        Entity selected = getWorld().getEntityByID(id);
                        boolean foundAction = false;
                        if (selected instanceof Unit) {
                            for (Entity ent : entsAt) {
                                if (((Unit)selected).canAttack(ent)) {
                                    server.send(new AttackAction(id, ent.id));
                                    foundAction = true;
                                    break;
                                }
                            }

                            if (!foundAction) {
                                for (Entity ent : entsAt) {
                                    if (ent instanceof Resource) {
                                        server.send(new GatherAction(id, ent.id));
                                        foundAction = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!foundAction) {
                            server.send(new MoveAction(id, pt));
                        }
                    }
                }
            }

            mousePressX = -1;
            mousePressY = -1;
            mousePressType = -1;
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
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                for (Integer id : selectedIds) {
                    Entity selected = getWorld().getEntityByID(id);
                    if (selected instanceof Building) {
                        Building selectedB = (Building)selected;
                        if (selectedB.getPlayerNumber() == playerNumber) {
                            try {
                                UnitTemplate toTrain = selectedB.trainableUnits()[0];
                                // This isn't a foolproof check, so we have to check serverside before doing the action as well.
                                // But, it means we can easily show them a failure message client side *most* of the time.
                                if (world.getPlayer(playerNumber).getNumCredits() >= toTrain.getCost().creditCost) {
                                    server.send(new TrainAction(id, toTrain));
                                } else {
                                    // Show not enough credits to user TODO
                                }
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                // This is fine; just means we can't train anything
                            }
                        }
                    }
                }
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

    /**
     * Selects the entities which can be selected out of the given list.
     * - Does not select entities which can't be selected.
     * - Does not select entities belonging to other players.
     * - Does not select more than one type of entity (e.g. both buildings and units; preference given to units).
     */
    private void trySelect(Entity[] toTry) {
        java.util.List<Entity> toSelectBuildings = new ArrayList<Entity>();
        java.util.List<Entity> toSelectUnits = new ArrayList<Entity>();
        for (Entity e : toTry) {
            if (e instanceof OwnedEntity) {
                boolean valid = true;
                OwnedEntity oe = (OwnedEntity)e;
                if (oe.getPlayerNumber() != playerNumber)
                    valid = false;
                else
                    valid = true;

                if (valid) {
                    if (e instanceof Unit)
                        toSelectUnits.add(e);
                    else if (e instanceof Building)
                        toSelectBuildings.add(e);
                    else
                        throw new IllegalStateException("Owned entity found which was neither building nor unit: " + e.getClass().getCanonicalName());
                }
            }
        }

        if (!toSelectUnits.isEmpty()) {
            select(toSelectUnits.toArray(new Entity[0]));
        } else if (!toSelectBuildings.isEmpty()) {
            select(toSelectBuildings.toArray(new Entity[0]));
        }
    }

    private void select(Entity[] toSelect) {
        selectedIds.clear();
        for (Entity e : toSelect) {
            selectedIds.add(e.id);
        }

        checkSelectedPanel();
    }

    private void checkSelectedPanel() {
        selectionPanel.removeAll();

        // Draw the selected panel for one of the selected buildings
        Building[] selected = getSelectedBuildings();
        Building toDraw = null;
        for (Building b : selected) {
            if (b.getPlayerNumber() == playerNumber) {
                toDraw = b;
                break;
            }
        }

        if (toDraw == null ) {
            return; // We don't *need* to draw anything
        }

        for (Action a : toDraw.getActions()) {
            selectionPanel.add(new ActionButton(server, a));
        }
        selectionPanel.revalidate();
        selectionPanel.repaint();
    }

    private Building[] getSelectedBuildings() {
        java.util.List<Building> buildings = new ArrayList<Building>();
        for (Integer i : getSelected()) {
            Entity e = world.getEntityByID(i);
            if (e instanceof Building) {
                buildings.add((Building)e);
            }
        }
        return buildings.toArray(new Building[0]);
    }
}
