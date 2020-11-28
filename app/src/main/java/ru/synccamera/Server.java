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

    public Server(int port) {
        ServerSocketCreator serverSocketCreator = new ServerSocketCreator(port);
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
            Socket socket = connectionEstablisher.getSocket();
            if (socket != null) {
                senderReceiver.add(new SenderReceiver(socket));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfConnections() {
        return senderReceiver.size();
    }

    static class ServerSocketCreator extends Thread {

        private int port;
        private volatile ServerSocket serverSocket;

        public ServerSocketCreator(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setSoTimeout(500);
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
                Log.d("SyncCamera", "Failed to open server socket");
            }
        }

        public Socket getSocket() {
            return socket;
        }

    }
}
