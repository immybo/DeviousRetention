package network;

import controller.Action;
import model.Entity;
import model.Player;
import view.Client;
import model.World;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * A client-to-server network connection
 */
public class CTSConnection {
    public final static int CONNECT_PORT = 57645;

    private final Client client;
    private Socket server;
    private Thread listenThread;
    private ObjectOutputStream out;

    public CTSConnection(Client client, Player player) {
        this.client = client;
        this.server = null;
        connect(player);

        Runnable listenRun = new Runnable() {
            public void run() {
                listen(server);
            }
        };
        listenThread = new Thread(listenRun);
        listenThread.start();
    }

    public void connect(Player player) {
        try {
            this.server = new Socket(InetAddress.getByName("localhost"), STCConnection.LISTEN_PORT);
            out = new ObjectOutputStream(server.getOutputStream());
            send(player);
        } catch (IOException e) {
            System.err.println("couldn't accept server connection: " + e);
            System.exit(1);
        }
    }

    public void listen(Socket server) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(server.getInputStream());
        } catch (IOException e) {
            System.err.println("couldn't listen to client connection: " + e);
            System.exit(1);
        }

        while (!server.isClosed()) {
            try {
                Object in = input.readObject();
                if (in instanceof World) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            client.setWorld((World)in);
                        }
                    });
                } else if (in instanceof Entity[]) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            client.updateEntities((Entity[])in);
                        }
                    });
                } else if (in instanceof Player[]) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            client.updatePlayers((Player[])in);
                        }
                    });
                }
            } catch (IOException|ClassNotFoundException e) {
                System.err.println("couldn't read object from client: " + e);
                // No need to terminate here, we can keep going
            }
        }
    }

    public void send(Object o) {
        try {
            out.writeObject(o);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("unable to send object to server: " + e);
        }
    }
}
