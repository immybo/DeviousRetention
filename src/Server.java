import model.World;
import model.entity.Unit;
import network.CTSConnection;
import network.STCConnection;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the game. Note that a server doesn't necessarily have to be
 * over the network; a server and client can exist on the same computer.
 */
public class Server implements CTSConnection {
    private List<STCConnection> clients;
    private World world;

    public Server(World world) {
        clients = new ArrayList<STCConnection>();
        this.world = world;
    }

    public void addClient(STCConnection connection) {
        clients.add(connection);
    }

    public void updateClients() {
        for (STCConnection client : clients) {
            client.setWorld(world);
        }
    }

    @Override
    public void moveTo(Unit unit, Point.Double worldPosition) {
        // Just assume that they're allowed to for now
        unit.setMovePoint(worldPosition);
    }
}
