package ru.synccamera;

import android.os.AsyncTask;
import android.os.Handler;
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
                    System.out.println(buffer.toString());
                    if (handler != null) handler.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        Log.d("SyncCamera","End of receiving");
    }

    public void write(byte[] bytes) {
        new AsyncWriter(bytes, outputStream).execute();
    }

    public void setCallback(Handler.Callback callback) {
        handler = new Handler(callback);
    }

    static class AsyncWriter extends AsyncTask {

        private byte[] bytes;
        private OutputStream stream;

        public AsyncWriter(byte[] bytes, OutputStream stream) {
            this.bytes = bytes;
            this.stream = stream;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                stream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
