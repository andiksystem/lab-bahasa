/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.andikhermawan.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andikhermawan
 */
public class CommandServer extends Thread {

    private boolean started;

    private ServerSocket serverSocket;

    public CommandServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            started = false;
        }
    }

    @Override
    public void run() {
        started = true;
        Socket socket = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            while (started) {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                dataInputStream = new DataInputStream(inputStream);
                dataOutputStream = new DataOutputStream(outputStream);

                String command = dataInputStream.readUTF();
                System.out.println("execute command " + command);

                dataOutputStream.writeUTF(command);
                dataOutputStream.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            started = false;
            if (socket != null) try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (dataInputStream != null) try {
                dataInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (dataOutputStream != null) try {
                dataOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (outputStream != null) try {
                outputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
