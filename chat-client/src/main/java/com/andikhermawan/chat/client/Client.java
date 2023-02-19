package com.andikhermawan.chat.client;

import com.andikhermawan.chat.commons.Message;
import com.andikhermawan.chat.commons.Utils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.sound.sampled.LineUnavailableException;

public class Client extends Thread {

    private final Socket socket;
    private final ArrayList<AudioChannel> channels;
    private MicThread micThread;

    public Client(String serverIp, int serverPort) throws UnknownHostException, IOException {
        channels = new ArrayList<>();
        socket = new Socket(serverIp, serverPort);
    }

    @Override
    public void run() {
        try {
            ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
            try {
                Utils.sleep(100);
                micThread = new MicThread(toServer);
                micThread.start();
            } catch (LineUnavailableException e) {
                System.out.println("mic unavailable " + e);
            }

            while (true) {
                if (socket.getInputStream().available() > 0) {
                    Message in = (Message) (fromServer.readObject()); 
                    
                    AudioChannel sendTo = null;
                    for (AudioChannel ch : channels) {
                        if (ch.getChId() == in.getChatId()) {
                            sendTo = ch;
                        }
                    }
                    if (sendTo != null) {
                        sendTo.addToQueue(in);
                    } else { 
                        AudioChannel ch = new AudioChannel(in.getChatId());
                        ch.addToQueue(in);
                        ch.start();
                        channels.add(ch);
                    }
                } else { 
                    ArrayList<AudioChannel> killMe = new ArrayList<>();
                    for (AudioChannel c : channels) {
                        if (c.canKill()) {
                            killMe.add(c);
                        }
                    }
                    for (AudioChannel c : killMe) {
                        c.closeAndKill();
                        channels.remove(c);
                    }
                    Utils.sleep(1);
                }
            }

        } catch (IOException | ClassNotFoundException e) { 
            System.out.println("client err " + e.toString());
        }
    }
}
