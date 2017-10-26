package util;

import java.awt.*;

public class CoordinateTranslation {
    private int xOffset;
    private int yOffset;
    private double xMultiplier;
    private double yMultiplier;


    public CoordinateTranslation(int xOffset, int yOffset, double xMultiplier, double yMultiplier) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xMultiplier = xMultiplier;
        this.yMultiplier = yMultiplier;
    }

    public Point toScreenCoordinates(Point.Double initialPoint) {
        return new Point((int)(initialPoint.x*xMultiplier) + xOffset, (int)(initialPoint.y*yMultiplier) + yOffset);
    }

    public Rectangle toScreenCoordinates(Rectangle.Double initialBounds) {
        Point topLeft = toScreenCoordinates(new Point.Double(initialBounds.x, initialBounds.y));
        Point bottomRight = toScreenCoordinates(new Point.Double(initialBounds.getMaxX(), initialBounds.getMaxY()));
        return new Rectangle(topLeft.x, topLeft.y, bottomRight.x-topLeft.x, bottomRight.y-topLeft.y);
    }

    public Point.Double toWorldCoordinates(Point initialPoint) {
        return new Point.Double((double)(initialPoint.x - xOffset) / xMultiplier, (double)(initialPoint.y - yOffset) / yMultiplier);
    }

    public Point getWorldToScreenMultiplier() {
        return new Point((int)(xMultiplier), (int)(yMultiplier));
    }
}
