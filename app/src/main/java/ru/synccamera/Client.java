package ru.synccamera;

import android.os.Handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {

    private final int port = 8888;
    Socket socket;
    InetAddress hostAddress;
    SenderReceiver senderReceiver;
    Handler.Callback callback;

    public Client(InetAddress hostAddress, Handler.Callback callback) {
        this.hostAddress = hostAddress;
        socket = new Socket();
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAddress, port), 5000);
            senderReceiver = new SenderReceiver(socket);
            senderReceiver.setCallback(callback);
            senderReceiver.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
