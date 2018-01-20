package model;

import controller.Cost;
import model.entity.BuildingTemplate;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Robert Campbell on 26/11/2017.
 */
public class Player implements Serializable {
    private final static Color[] playerColors = {
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW
    };

    private final int playerNumber;
    private final Color playerColor;
    private int numCredits;
    private final BuildingTemplate[] buildable;

    public Player(int playerNumber, BuildingTemplate[] buildable) {
        if (playerNumber < 0 || playerNumber >= playerColors.length) {
            throw new IllegalArgumentException("Player number of " + playerNumber + " not allowed; must be >0 and <" + playerColors.length);
        }

        this.playerNumber = playerNumber;
        this.playerColor = playerColors[playerNumber];
        this.buildable = buildable;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public static Color getPlayerColor(int playerNumber) {
        return playerColors[playerNumber];
    }

    public int getNumCredits() {
        return numCredits;
    }

    public void earnCredits(int num) {
        numCredits += num;
    }

    public boolean spendCredits(Cost cost) {
        if (getNumCredits() < cost.creditCost) {
            return false;
        }

        spendCreditsNoCheck(cost.creditCost);
        return true;
    }

    public void spendCreditsNoCheck(int num) {
        numCredits -= num;
    }

    public BuildingTemplate[] getBuildable() {
        return buildable;
    }
}
