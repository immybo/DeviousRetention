package view;

import controller.AttackAction;
import controller.BuildAction;
import controller.GatherAction;
import controller.MoveAction;
import model.Entity;
import model.Player;
import model.entity.*;
import util.CoordinateTranslation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * Created by Robert Campbell on 20/01/2018.
 */
public class GamePanel extends JPanel {
    private final Client client;

    // To calculate how big & where the tiles are
    private double xOffset;
    private double yOffset;
    private int tileSize;
    private double zoom;

    // For mouse dragging
    private int mousePressX = -1;
    private int mousePressY = -1;

    private int mousePressType = -1;

    private int mouseX = -1;
    private int mouseY = -1;

    private boolean isPlacingBuilding = false;
    private BuildingTemplate placingBuildingType = null;

    public GamePanel(Client client) {
        super();

        // Defaults; these are changed on scroll/zoom
        this.xOffset = 0;
        this.yOffset = 0;
        this.zoom = 1;
        this.tileSize = 200;

        this.addMouseListener(new GamePanelMouseListener());
        this.addMouseMotionListener(new GamePanelMouseMotionListener());

        this.client = client;
    }

    public void moveViewBy(double x, double y) {
        xOffset += x;
        yOffset += y;
    }

    public void zoomBy(double factor) {
        zoom *= factor;
        if (zoom < 0.1) {
            zoom = 0.1;
        } else if (zoom > 10) {
            zoom = 10;
        }
    }

    /**
     * Makes the next left-click on the board area place a specific building if
     * said building would not collide and if this is not cancelled.
     */
    public void setPlacingBuilding(BuildingTemplate bt) {
        this.isPlacingBuilding = true;
        this.placingBuildingType = bt;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        CoordinateTranslation translation = getCoordinateTranslation();
        client.getWorld().renderOn(g2d, translation);

        // Draw a box around all of the selected entities
        Stroke oldStroke = g2d.getStroke();
        for (Integer i : client.getSelected()) {
            g2d.setStroke(new BasicStroke(5));
            Entity e = client.getWorld().getEntityByID(i);
            if (e instanceof OwnedEntity) {
                OwnedEntity ownedE = (OwnedEntity)e;
                g2d.setColor(Player.getPlayerColor(ownedE.getPlayerNumber()));
                Rectangle.Double bounds = e.getBounds();
                Rectangle screenBounds = translation.toScreenCoordinates(bounds);
                g2d.drawRect(screenBounds.x, screenBounds.y, screenBounds.width, screenBounds.height);

                if (e instanceof Unit) {
                    Unit u = (Unit)e;
                    if (u.can(Entity.Ability.ATTACK)) {// Draw a circle around us to indicate range
                        int radiusX = (int)(translation.getWorldToScreenMultiplier().getX() * u.getRange());
                        int radiusY = (int)(translation.getWorldToScreenMultiplier().getY() * u.getRange());
                        Point center = translation.toScreenCoordinates(new Point.Double(u.getX(), u.getY()));

                        g2d.setColor(Player.getPlayerColor(u.getPlayerNumber()));
                        g2d.setStroke(new BasicStroke(1));
                        g2d.drawOval(center.x - radiusX, center.y - radiusY, radiusX * 2, radiusY * 2);
                    }
                }
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

    private CoordinateTranslation getCoordinateTranslation() {
        return new CoordinateTranslation((int)(xOffset*tileSize*zoom), (int)(yOffset*tileSize*zoom), tileSize*zoom, tileSize*zoom);
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
                if (oe.getPlayerNumber() != client.getPlayerNumber())
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
            client.select(toSelectUnits.toArray(new Entity[0]));
        } else if (!toSelectBuildings.isEmpty()) {
            client.select(toSelectBuildings.toArray(new Entity[0]));
        }
    }

    private class GamePanelMouseListener implements MouseListener {
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
            if (e.getButton() == MouseEvent.BUTTON1 && isPlacingBuilding) {
                Point.Double pressPt = getCoordinateTranslation().toWorldCoordinates(new Point(mousePressX, mousePressY));
                client.sendAction(new BuildAction(placingBuildingType, client.getPlayerNumber(), pressPt.getX(), pressPt.getY()));
                isPlacingBuilding = false;
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                if (mousePressX == -1 || mousePressType != MouseEvent.BUTTON1) {
                    Entity underMouse = client.getWorld().getEntityAt(pt);
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


                    Entity[] underMouse = client.getWorld().getEntitiesIn(new Rectangle.Double(left, top, width, height));
                    trySelect(underMouse);

                    mousePressX = -1;
                    mousePressY = -1;
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                Entity[] entsAt = client.getWorld().getEntitiesAt(pt);
                for (Integer id : client.getSelected()) {
                    Entity selected = client.getWorld().getEntityByID(id);
                    boolean foundAction = false;
                    if (selected instanceof Unit) {
                        for (Entity ent : entsAt) {
                            if (((Unit)selected).canAttack(ent)) {
                                client.sendAction(new AttackAction(id, ent.id));
                                foundAction = true;
                                break;
                            }
                        }

                        if (!foundAction) {
                            for (Entity ent : entsAt) {
                                if (ent instanceof Resource) {
                                    client.sendAction(new GatherAction(id, ent.id));
                                    foundAction = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!foundAction) {
                        client.sendAction(new MoveAction(id, pt));
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

    private class GamePanelMouseMotionListener implements MouseMotionListener {
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
}
