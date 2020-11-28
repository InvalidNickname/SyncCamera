package ru.synccamera;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SenderReceiver extends Thread {

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private Handler handler;

    public SenderReceiver(Socket socket) {
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;
        while (socket != null) {
            try {
                bytes = inputStream.read(buffer);
                if (bytes > 0) {
                    Log.d("SyncCamera", "Got new message, sending to handler");
                    if (handler != null) {
                        Message message = handler.obtainMessage(1, bytes, -1, buffer);
                        message.sendToTarget();
                    }
                }
            } catch (IOException e) {
                Log.d("SyncCamera", "Error while receiving messages");
                //e.printStackTrace();
            }
        }
        Log.d("SyncCamera", "End of receiving");
    }

    public void write(byte[] bytes) {
        new AsyncWriter(bytes, outputStream).execute();
    }

    public void setCallback(Handler handler) {
        this.handler = handler;
    }

    @SuppressWarnings("deprecation")
    static class AsyncWriter extends AsyncTask<Void, Void, Void> {

        private byte[] bytes;
        private OutputStream stream;

        public AsyncWriter(byte[] bytes, OutputStream stream) {
            this.bytes = bytes;
            this.stream = stream;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String string = new String(bytes);
            Log.d("SyncCamera", "Trying to send \"" + string + "\"");
            try {
                stream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("SyncCamera", "Sent \"" + string + "\"");
            return null;
        }
    }
}
