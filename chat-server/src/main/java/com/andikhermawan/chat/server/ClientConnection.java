package com.andikhermawan.chat.server;

import com.andikhermawan.chat.commons.Message;
import com.andikhermawan.chat.commons.SoundPacket;
import com.andikhermawan.chat.commons.Utils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection extends Thread {

    private boolean started;
    private final Server server;
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private final long chatId;
    private final ArrayList<Message> toSend;

    public ClientConnection(Server serv, Socket socket) {
        started = true;
        this.toSend = new ArrayList<>();
        this.server = serv;
        this.socket = socket;
        byte[] addr = socket.getInetAddress().getAddress();
        this.chatId = (addr[0] << 48 | addr[1] << 32 | addr[2] << 24 | addr[3] << 16) + socket.getPort();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public long getChatId() {
        return chatId;
    }

    public void addToQueue(Message m) {
        try {
            toSend.add(m);
        } catch (Throwable t) {

        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            try {
                socket.close();
                Log.add("ERROR " + getInetAddress() + ":" + getPort() + " " + ex);
            } catch (IOException ex1) {
            }
            started = false;
        }

        while (started) {
            try {
                if (socket.getInputStream().available() > 0) {
                    Message toBroadcast = (Message) in.readObject();
                    if (toBroadcast.getChatId() == -1) {
                        toBroadcast.setChId(chatId);
                        toBroadcast.setTimestamp(System.nanoTime() / 1000000L);
                        server.addToBroadcastQueue(toBroadcast);
                    } else {
                        continue;
                    }
                }
                
                try {
                    if (!toSend.isEmpty()) {
                        Message toClient = toSend.get(0);
                        if (!(toClient.getData() instanceof SoundPacket) || toClient.getTimestamp() + toClient.getTtl() < System.nanoTime() / 1000000L) { 
                            Log.add("dropping packet from " + toClient.getChatId() + " to " + chatId);
                            continue;
                        }
                        out.writeObject(toClient);
                        toSend.remove(toClient);
                    } else {
                        Utils.sleep(10);
                    }
                } catch (Throwable t) {
                    if (t instanceof IOException) {
                        throw (Exception) t;
                    } else {
                        System.out.println("cc fixmutex");
                    }
                }
            } catch (Exception ex) {
                try {
                    socket.close();
                } catch (IOException ex1) {
                }
                started = false;
            }
        }

    }
}
