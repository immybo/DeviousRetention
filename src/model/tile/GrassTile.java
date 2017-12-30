package model.tile;

import model.Tile;

import java.io.Serializable;

public class GrassTile implements Tile, Serializable {
    public boolean collides() {
        return false;
    }
}
