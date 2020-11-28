package ru.synccamera;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {

    Socket socket;
    InetAddress hostAddress;
    SenderReceiver senderReceiver;
    Handler handler;
    private int port;

    public Client(int port, InetAddress hostAddress, Handler handler) {
        this.port = port;
        this.hostAddress = hostAddress;
        socket = new Socket();
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress, port), 500);
            Log.d("SyncCamera", "Client connected to " + hostAddress + ":" + port);
            senderReceiver = new SenderReceiver(socket);
            senderReceiver.setCallback(handler);
            senderReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
