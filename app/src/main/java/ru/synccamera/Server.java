package ru.synccamera;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    private final int port = 8888;
    private Socket socket;
    private ServerSocket serverSocket;
    private SenderReceiver senderReceiver;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            senderReceiver = new SenderReceiver(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] bytes) {
        senderReceiver.write(bytes);
    }
}
