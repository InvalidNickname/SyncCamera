package ru.synccamera;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<SenderReceiver> senderReceiver = new ArrayList<>();
    private ServerSocket serverSocket;

    public Server() {
        ServerSocketCreator serverSocketCreator = new ServerSocketCreator();
        serverSocketCreator.start();
        try {
            serverSocketCreator.join();
            serverSocket = serverSocketCreator.getServerSocket();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] bytes) {
        Log.d("SyncCamera", String.valueOf(senderReceiver.size()));
        for (SenderReceiver receiver : senderReceiver) {
            if (receiver != null) {
                receiver.write(bytes);
            } else {
                Log.d("SyncCamera", "Server socket isn't ready");
            }
        }
    }

    public void newConnection() {
        ConnectionEstablisher connectionEstablisher = new ConnectionEstablisher(serverSocket);
        connectionEstablisher.start();
        try {
            connectionEstablisher.join();
            senderReceiver.add(new SenderReceiver(connectionEstablisher.getSocket()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ServerSocketCreator extends Thread {

        private final int port = 8888;
        private volatile ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ServerSocket getServerSocket() {
            return serverSocket;
        }
    }


    static class ConnectionEstablisher extends Thread {

        private volatile Socket socket;
        private ServerSocket serverSocket;

        public ConnectionEstablisher(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        @Override
        public void run() {
            newConnection();
        }

        public void newConnection() {
            try {
                socket = serverSocket.accept();
                Log.d("SyncCamera", "Server socket opened");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Socket getSocket() {
            return socket;
        }

    }
}
