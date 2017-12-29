package controller;

import model.Entity;
import model.World;
import model.entity.Unit;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Robert Campbell on 14/10/2017.
 */
public class MoveAction extends Action {
    private final int id;
    private final Point.Double newPoint;

    public MoveAction(int id, Point.Double newPoint) {
        this.id = id;
        this.newPoint = newPoint;
    }

    @Override
    public void run(World world) {
        Entity e = world.getEntityByID(id);
        if (e instanceof Unit) {
            ((Unit)e).setMovePoint(newPoint);
        }
    }
}
