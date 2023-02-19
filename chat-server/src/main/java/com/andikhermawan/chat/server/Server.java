package com.andikhermawan.chat.server;

import com.andikhermawan.chat.commons.Message;
import com.andikhermawan.chat.commons.Utils;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private ArrayList<Message> broadCastQueue;
    private ArrayList<ClientConnection> clients;

    private int port;
    private ServerSocket serverSocket;
    private MainFrame mainFrame;

    public Server(MainFrame mainFrame, int port) throws Exception {
        this.mainFrame = mainFrame;
        this.broadCastQueue = new ArrayList<>();
        this.clients = new ArrayList<>();
        this.port = port;

        try {
            serverSocket = new ServerSocket(port);
            Log.add("Port " + port + ": server started");
        } catch (IOException ex) {
            Log.add("Server error " + ex + "(port " + port + ")");
            throw new Exception("Error " + ex);
        }

        BroadcastThread broadcastThread = new BroadcastThread();
        broadcastThread.start();

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection clientConnection = new ClientConnection(this, socket);
                clientConnection.start();
                addToClients(clientConnection);
                Log.add("new client " + socket.getInetAddress() + ":" + socket.getPort() + " on port " + port + ", clientId: " + clientConnection.getChatId());
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    public void addToBroadcastQueue(Message message) {
        try {
            broadCastQueue.add(message);
        } catch (Throwable t) {
            Utils.sleep(1);
            addToBroadcastQueue(message);
        }
    }

    private void addToClients(ClientConnection clientConnection) {
        try {
            clients.add(clientConnection);
        } catch (Throwable t) {
            Utils.sleep(1);
            addToClients(clientConnection);
        }
    }

    private class BroadcastThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    ArrayList<ClientConnection> toRemove = new ArrayList<>();
                    for (ClientConnection clientConnection : clients) {
                        if (!clientConnection.isAlive()) {
                            Log.add("dead connection closed: " + clientConnection.getInetAddress() + ":" + clientConnection.getPort() + " on port " + port);
                            toRemove.add(clientConnection);
                        }
                    }

                    clients.removeAll(toRemove);
                    if (broadCastQueue.isEmpty()) {
                        Utils.sleep(10);
                    } else {
                        Message m = broadCastQueue.get(0);
                        for (ClientConnection clientConnection : clients) {
                            if (clientConnection.getChatId() != m.getChatId()) {
                                clientConnection.addToQueue(m);
                            }
                        }
                        broadCastQueue.remove(m);
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, null, t);
                }
            }
        }
    }

}
