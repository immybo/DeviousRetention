package controller;

import model.Player;
import model.World;
import model.entity.Unit;
import network.STCConnection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the game. Note that a server doesn't necessarily have to be
 * over the network; a server and client can exist on the same computer.
 */
public class Server {
    private List<STCConnection> clients;
    private World world;

    private int i = 0;

    public Server(World world) {
        clients = new ArrayList<STCConnection>();
        this.world = world;
    }

    public void addClient(STCConnection connection) {
        clients.add(connection);
    }

    public World getWorld() {
        return world;
    }

    public void addPlayer(Player player) {
        world.addPlayer(player);
    }

    public void updateClients() {
        if (world != null) {
            for (STCConnection client : clients) {
                client.send(world.getEntities());
                client.send(world.getPlayers());
            }
        }
    }

    public void processAction(Action a) {
        a.run(world);
    }
}
