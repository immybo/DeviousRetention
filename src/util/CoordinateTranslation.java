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

    public Point getTransformedPoint(Point.Double initialPoint) {
        return new Point((int)(initialPoint.x*xMultiplier) + xOffset, (int)(initialPoint.y*yMultiplier) + yOffset);
    }
}
