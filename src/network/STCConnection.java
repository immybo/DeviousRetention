package network;

import controller.Action;
import controller.Server;
import model.Entity;
import model.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * A server-to-client network connection;
 */
public class STCConnection {
    public static final int LISTEN_PORT = 62554;

    private final Server server;
    private Socket client;
    private Thread listenThread;
    private ObjectOutputStream out;

    public STCConnection(Server server) {
        this.server = server;
        this.client = null;
        this.listenThread = null;

        Runnable listenRun = new Runnable() {
            public void run() {
                listen();
            }
        };
        this.listenThread = new Thread(listenRun);
        listenThread.start();
    }

    private void listen() {
        try {
            ServerSocket listener = new ServerSocket(LISTEN_PORT);
            this.client = listener.accept();
            this.out = new ObjectOutputStream(client.getOutputStream());
            listener.close();

            Runnable listenRun = new Runnable() {
                public void run() {
                    listenToClient(client);
                }
            };
            new Thread(listenRun).start();
        } catch (IOException e) {
            System.err.println("couldn't accept client connection: " + e);
            System.exit(1);
        }
    }

    private void listenToClient(Socket client) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            System.err.println("couldn't listen to client connection: " + e);
            System.exit(1);
        }

        while (!client.isClosed()) {
            try {
                Action in = (Action)input.readObject();
                server.processAction(in);
            } catch (IOException|ClassNotFoundException e) {
                System.err.println("couldn't read object from client: " + e);
                // No need to terminate here, we can keep going
            }
        }
    }

    public void send(Object o){
        try {
            out.writeObject(o);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.err.println("unable to send object to clients: " + e);
        }
    }
}
