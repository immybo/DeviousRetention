package controller;

import model.World;

import java.io.Serializable;

public class TickObject implements Serializable {
    public final long tickNumber;
    public final Action[] actions;
    public final int nextID;
    private final int previousWorldHash;
    // Whether or not the client should reset to this tick number, rather than checking against it.
    // There should have been a world sent immediately ahead of this if this field is true
    private final boolean shouldReset;

    public TickObject(long tickNumber, Action[] actions, int previousWorldHash, boolean shouldReset, int nextID) {
        this.tickNumber = tickNumber;
        this.actions = actions;
        this.previousWorldHash = previousWorldHash;
        this.shouldReset = shouldReset;
        this.nextID = nextID;
    }

    public void apply(World world) {
        for (Action a : actions) {
            a.run(world);
        }
    }

    public int getPreviousWorldHash() {
        return previousWorldHash;
    }

    public boolean shouldReset() {
        return shouldReset;
    }
}
