package model;

/**
 * Represents everything on the board, e.g. the entities and
 * the tiles.
 */
public class World {
    private final Board board;

    public World(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }
}
