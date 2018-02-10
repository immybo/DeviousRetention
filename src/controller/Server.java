package controller;

import model.Player;
import model.World;
import model.entity.Unit;
import network.STCConnection;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controls the game. Note that a server doesn't necessarily have to be
 * over the network; a server and client can exist on the same computer.
 */
public class Server {
    public static final int LISTEN_PORT = 62554;
    private Thread listenThread;

    private List<STCConnection> clients;
    private World world;

    private int i = 0;

    public Server(World world) {
        clients = new CopyOnWriteArrayList<STCConnection>();
        this.world = world;

        this.listenThread = null;

        Runnable listenRun = new Runnable() {
            public void run() {
                try {
                    listenForClients();
                } catch (IOException e) {
                    System.err.println("couldn't open server socket: " + e);
                    System.exit(1);
                }
            }
        };
        this.listenThread = new Thread(listenRun);
        listenThread.start();
    }

    private void listenForClients() throws IOException {
        ServerSocket listener = new ServerSocket(LISTEN_PORT);
        while(true) {
            try {
                Socket clientSocket = listener.accept();
                STCConnection clientConnection = new STCConnection(this, clientSocket);
                clientConnection.send(getWorld());
                addClient(clientConnection);
            } catch (IOException e) {
                System.err.println("couldn't accept client connection: " + e);
                System.exit(1);
            }
        }
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
