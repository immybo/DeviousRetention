package view;

import controller.*;
import controller.Action;
import model.Entity;
import model.Player;
import model.World;
import model.entity.*;
import network.CTSConnection;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Client {
    private static final double KEY_MOVEMENT_SPEED = 1;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10;
    private static final double ZOOM_CHANGE_MULTIPLIER = 1.2;
    private static final Font CREDITS_FONT = new Font("Arial", Font.BOLD, 50);

    private JFrame frame;
    private GamePanel gamePanel;
    private JPanel infoPanel;
    private JPanel buildingsPanel;
    private JPanel selectionPanel;
    private AtomicReference<World> world;
    private CTSConnection server = null;

    private java.util.List<Integer> selectedIds;

    private final int playerNumber;

    public Client(World world, int playerNumber) {
        this.world = new AtomicReference<World>(world);
        this.selectedIds = new ArrayList<Integer>();
        this.playerNumber = playerNumber;

        frame = new JFrame();

        gamePanel = new GamePanel(this);

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
                if (getWorld().getPlayer(playerNumber) != null) {
                    g2d.drawString("Credits: " + getWorld().getPlayer(playerNumber).getNumCredits(), x, y);
                }

                y += 200;
            }
        };

        buildingsPanel = new JPanel();

        selectionPanel = new JPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(500, 1080));
        rightPanel.setLayout(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(500, 800));
        rightPanel.add(infoPanel, BorderLayout.NORTH);
        rightPanel.add(buildingsPanel, BorderLayout.CENTER);
        rightPanel.add(selectionPanel, BorderLayout.SOUTH);

        frame.addKeyListener(new ClientKeyListener());
        frame.addMouseWheelListener(new ClientMouseWheelListener());

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(gamePanel, BorderLayout.CENTER);
        pane.add(rightPanel, BorderLayout.EAST);
        frame.setMinimumSize(new Dimension(1920, 1080));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void sendAction(Action a) {
        if (server == null)
            throw new IllegalStateException("Client is not connected to server, but we tried to send an action: " + a);
        server.send(a);
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setServer(CTSConnection server) {
        this.server = server;
    }

    public void setWorld(World world) {
        this.world.set(world);
        if (buildingsPanel.getComponents().length == 0) {
            buildingsPanel.removeAll();
            for (BuildingTemplate bt : getWorld().getPlayer(playerNumber).getBuildable()) {
                JButton buildButton = new JButton(bt.getName());
                buildButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        gamePanel.setPlacingBuilding(bt);
                    }
                });
                buildingsPanel.add(buildButton);
            }
            frame.validate();
        }
        frame.repaint();
    }

    public void updateEntities(Entity[] entities) {
        world.get().setEntities(entities);
        frame.repaint();
    }

    public World getWorld() {
        return world.get();
    }

    public Integer[] getSelected() {
        // Make sure that they all still exist
        java.util.List<Integer> toRemove = new ArrayList<Integer>();
        for (Integer i : toRemove) {
            try {
                world.get().getEntityByID(i);
            } catch (IllegalArgumentException e) {
                toRemove.add(i);
            }
        }

        for (Integer i : toRemove) {
            selectedIds.remove(i);
        }

        return selectedIds.toArray(new Integer[0]);
    }

    /**
     * Key listener for the entire client. We want this to apply to the entire client since
     * the focus may not be on any individual element of it.
     */
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
                gamePanel.moveViewBy(KEY_MOVEMENT_SPEED, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                gamePanel.moveViewBy(-KEY_MOVEMENT_SPEED, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                gamePanel.moveViewBy(0, KEY_MOVEMENT_SPEED);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                gamePanel.moveViewBy(0, -KEY_MOVEMENT_SPEED);
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
                                if (getWorld().getPlayer(playerNumber).getNumCredits() >= toTrain.getCost().creditCost) {
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
            gamePanel.zoomBy(Math.pow(ZOOM_CHANGE_MULTIPLIER, -rotAmount));
        }
    }

    public void select(Entity[] toSelect) {
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

        frame.validate();
    }

    private Building[] getSelectedBuildings() {
        java.util.List<Building> buildings = new ArrayList<Building>();
        for (Integer i : getSelected()) {
            Entity e = getWorld().getEntityByID(i);
            if (e instanceof Building) {
                buildings.add((Building)e);
            }
        }
        return buildings.toArray(new Building[0]);
    }

    /**
     * Places a specific building type at the given point
     */
    private void build(BuildingTemplate bt, double x, double y) {
        server.send(new BuildAction(bt, playerNumber, x, y));
    }
}
