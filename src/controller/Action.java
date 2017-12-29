package controller;

import model.World;

import java.io.Serializable;

/**
 * Created by Robert Campbell on 14/10/2017.
 */
public abstract class Action implements Serializable {
    public abstract void run(World world);

    public Cost getCost() {
        return new Cost(0);
    }
}
