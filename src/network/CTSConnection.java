package network;

import model.entity.Unit;

import java.awt.*;

/**
 * A client-to-server network connection
 */
public interface CTSConnection {
    public void moveTo(Unit unit, Point.Double worldPosition);
}
