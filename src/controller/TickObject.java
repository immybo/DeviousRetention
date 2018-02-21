package controller;

import model.World;

import java.io.Serializable;

public class TickObject implements Serializable {
    public final long tickNumber;
    public final Action[] actions;

    public TickObject(long tickNumber, Action[] actions) {
        this.tickNumber = tickNumber;
        this.actions = actions;
    }

    public void apply(World world) {
        for (Action a : actions) {
            a.run(world);
        }
    }
}
