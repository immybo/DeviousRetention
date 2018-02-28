package view;

import controller.Cost;
import controller.Server;
import model.*;
import model.entity.BuildingTemplate;
import model.entity.EntityTemplate;
import model.entity.ResourceTemplate;
import model.entity.UnitTemplate;
import model.tile.GrassTile;
import model.tile.MountainTile;
import network.CTSConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Menu {
    public static final int TICK_TIME_MS = 16;

    private JFrame frame;
    private JPanel panel;

    private int currentPlayerNumber;

    public static void main(String[] args) {
        new Menu();
    }

    public Menu() {
        currentPlayerNumber = 0;

        JButton startServerButton = new JButton("Start Server");
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        JLabel serverIPLabel = new JLabel("Server IP");
        JTextField serverIPField = new JTextField(20);
        serverIPField.setMaximumSize(new Dimension(200, 25));
        JButton connectButton = new JButton("Connect to Server");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startClient(InetAddress.getByName(serverIPField.getText()));
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
            }
        });

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(startServerButton);
        panel.add(serverIPLabel);
        panel.add(serverIPField);
        panel.add(connectButton);

        frame = new JFrame();
        frame.add(panel);
        frame.setMinimumSize(new Dimension(200, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Tile[][] tiles = new Tile[30][30];
                for (int i = 0; i < tiles.length; i++) {
                    for (int j = 0; j < tiles[0].length; j++) {
                        tiles[i][j] = new GrassTile();
                    }
                }

                for (int i = 12; i < 19; i++) {
                    for (int j = 12; j < 19; j++) {
                        tiles[i][j] = new MountainTile();
                    }
                }

                Board board = new Board(tiles);

                UnitTemplate gatherer = new UnitTemplate("Gatherer", new Entity.Ability[]{Entity.Ability.ATTACK, Entity.Ability.GATHER}, 0.2, 1, 10, 10, 0.5, 100, new Cost(150), "gatherer.png");
                UnitTemplate archer = new UnitTemplate("Archer", new Entity.Ability[]{Entity.Ability.ATTACK}, 1, 6, 5, 20, 0.5, 100, new Cost(200), "archer.png");
                UnitTemplate infantry = new UnitTemplate("Infantry", new Entity.Ability[]{Entity.Ability.ATTACK}, 0.3, 1, 10, 75, 0.7, 200, new Cost(200), "infantry.png");
                UnitTemplate cavalry = new UnitTemplate("Cavalry", new Entity.Ability[]{Entity.Ability.ATTACK}, 1.5, 1, 10, 75, 1.2, 350, new Cost(600), "cavalry.png");

                BuildingTemplate headquarters = new BuildingTemplate("Headquarters", new Entity.Ability[]{}, 0, 1, 0, 1.5, 5000, new Cost(5000), new String[]{"Gatherer"}, new int[]{10});
                BuildingTemplate barracks = new BuildingTemplate("Barracks", new Entity.Ability[]{}, 0, 1, 0, 1, 2500, new Cost(2000), new String[]{"Archer", "Infantry", "Cavalry"}, new int[]{20, 20, 50});

                ResourceTemplate bigResource = new ResourceTemplate("Large Gold Mine", 2, 10000, 1);
                ResourceTemplate smallResource = new ResourceTemplate("Small Gold Mine", 1, 1500, 1.5);

                EntityTemplate[] allTemplates = new EntityTemplate[] {
                        gatherer, archer, infantry, cavalry, headquarters, barracks, bigResource, smallResource
                };

                String[] defaultTemplates = new String[]{
                        "Gatherer", "Headquarters"
                };

                World world = new World(board, allTemplates, defaultTemplates);

                int bottom = world.getBoard().getHeight();
                int right = world.getBoard().getWidth();

                world.addEntity(headquarters.create(world, 0, 4, 4));
                world.addEntity(gatherer.create(world, 0, 4, 6));
                world.addEntity(gatherer.create(world, 0, 4, 7));

                world.addEntity(headquarters.create(world, 1, right - 4, bottom - 4));
                world.addEntity(gatherer.create(world, 1, right - 4, bottom - 6));
                world.addEntity(gatherer.create(world, 1, right - 4, bottom - 7));

                world.addEntity(bigResource.create(world, -1, 10, 10));
                world.addEntity(bigResource.create(world, -1, 20, 20));
                world.addEntity(smallResource.create(world, -1, 20, 10));
                world.addEntity(smallResource.create(world, -1, 10, 20));

                Server server = new Server(world);
                ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(()->{server.tick();}, 0, TICK_TIME_MS, TimeUnit.MILLISECONDS);
            }
        }).run();
    }

    private void startClient(InetAddress serverIP) {
        Player player = new Player(currentPlayerNumber++);
        player.earnCredits(10000);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Client client = new Client(World.NULL_WORLD, player.getPlayerNumber());
                CTSConnection cts = new CTSConnection(client, player, serverIP);
                client.setServer(cts);
            }
        });
    }
}
