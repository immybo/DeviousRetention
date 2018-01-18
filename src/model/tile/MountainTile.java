package model.tile;

import model.Tile;

import java.io.Serializable;

public class MountainTile implements Tile, Serializable {
    public boolean collides() {
        return true;
    }
}