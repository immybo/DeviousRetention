package network;

import controller.Action;
import controller.GameStateChange;
import controller.TickObject;
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

    private final InetAddress ip;

    private boolean isDead;
    private boolean isClosed;

    public CTSConnection(Client client, Player player, InetAddress ip) {
        this.ip = ip;

        this.isDead = false;
        this.isClosed = false;

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
            this.server = new Socket(ip, STCConnection.LISTEN_PORT);
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
            if (isClosed) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }

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
                } else if (in instanceof TickObject) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            client.handleServerTick((TickObject)in);
                        }
                    });
                } else if (in instanceof GameStateChange) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (!isDead) {
                                client.handleGameStateChange((GameStateChange) in);
                            }
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
        if (this.isDead || this.isClosed) {
            return;
        }

        try {
            out.writeObject(o);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("unable to send object to server: " + e);
        }
    }

    public void setDead() {
        this.isDead = true;
    }

    public void close() {
        this.isClosed = true;
    }
}
