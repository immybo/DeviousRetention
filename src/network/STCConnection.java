package network;

import model.World;

/**
 * A server-to-client network connection;
 */
public interface STCConnection {
    public void setWorld(World world);
}
