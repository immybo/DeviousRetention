package controller;

import model.Entity;
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
import java.util.Collections;
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

    private long tickNumber;

    private List<Action> actionQueue;

    public Server(World world) {
        clients = new CopyOnWriteArrayList<STCConnection>();
        tickNumber = 0;
        this.world = world;
        this.actionQueue = Collections.synchronizedList(new ArrayList<Action>());
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

    public void queueAction(Action a) {
        actionQueue.add(a);
    }

    private Action[] pollActions() {
        synchronized(actionQueue) {
            Action[] actions = actionQueue.toArray(new Action[0]);
            actionQueue.clear();
            return actions;
        }
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

    public void tick() {
        Action[] toApply = pollActions();
        TickObject tickObj = new TickObject(tickNumber, toApply, getWorld().hashCode(), false, getWorld().getNextEntityID());
        tickNumber++;

        for (STCConnection client : clients) {
            client.send(tickObj);
        }

        tickObj.apply(world);
        world.tick();
    }

    /**
     * Makes sure that the given hash corresponds to our world. If it doesn't, reset
     * all of the clients to our current world.
     */
    public void checkHash(int hash) {
        if (hash == -1) {
            System.out.println("Resetting clients to tick #" + tickNumber);
            updateClients();
            resetClientsToTick(tickNumber);
        }

        int serverHash = getWorld().hashCode();
        if (hash != serverHash) {
            System.out.println("Out of sync; client hash of " + hash + " != server hash of " + serverHash);
            updateClients();
            resetClientsToTick(tickNumber);
        }
    }

    public void addClient(STCConnection connection) {
        clients.add(connection);
        connection.send(getWorld());
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

    public void resetClientsToTick(long tickNumber) {
        for (STCConnection client : clients) {
            client.send(new TickObject(tickNumber, new Action[0], getWorld().hashCode(), true, getWorld().getNextEntityID()));
        }
    }
}
