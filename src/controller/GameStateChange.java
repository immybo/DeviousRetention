package controller;

import java.io.Serializable;

public class GameStateChange implements Serializable {
    public enum Type {
        WIN,
        LOSE
    }

    public final Type type;
    public final int playerNumber;

    public GameStateChange(Type type, int playerNumber) {
        this.type = type;
        this.playerNumber = playerNumber;
    }
}
