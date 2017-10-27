package model.entity;

import model.Entity;

import java.awt.*;

public abstract class OwnedEntity extends Entity {
    public static final Color[] PLAYER_COLORS = {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW};

    private int playerNumber;

    public OwnedEntity(double x, double y, double size, int playerNumber) {
        super(x, y, size);
        this.playerNumber = playerNumber;
    }

    public int getPlayer() {
        return playerNumber;
    }

    @Override
    public Color getPlayerColor() {
        if (playerNumber >= PLAYER_COLORS.length) {
            return super.getPlayerColor();
        }
        return PLAYER_COLORS[playerNumber];
    }
}
