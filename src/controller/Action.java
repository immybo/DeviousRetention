package controller;

import model.World;

import java.io.Serializable;

/**
 * Created by Robert Campbell on 14/10/2017.
 */
public interface Action extends Serializable {
    public void run(World world);
}
