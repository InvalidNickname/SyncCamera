package ru.synccamera;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("deprecation")
public class CameraFragment extends P2PFragment {

    private Camera camera;
    private CameraPreview cameraPreview;
    private MediaRecorder mediaRecorder;
    private boolean preparedMediaRecorder;
    private ImageView recordingMark;
    private File nextSavePath, prevSavePath;
    private boolean videoRecorded = false;
    private long firstSync, timeDiff = 0;
    private Menu menu;
    private TextView cameraSynchronizedText;

    public CameraFragment() {

    }

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            Log.d("CameraFragment", "Opening camera");
            c = Camera.open();
        } catch (Exception e) {
            Log.d("CameraFragment", "Failed to open camera");
        }
        return c;
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SyncCamera");
        Log.d("CameraFragment", mediaStorageDir.toString());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("CameraFragment", "Failed to create save directory");
                return null;
            }
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".mp4");
        return mediaFile;
    }

    protected void setDeviceName() {
        deviceName = Build.MODEL;
        try {
            deviceName = deviceName.replace("-CONTROLLER", "");
            Method m = manager.getClass().getMethod("setDeviceName", channel.getClass(), String.class, WifiP2pManager.ActionListener.class);
            m.invoke(manager, channel, deviceName, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.d("ControllerFragment", "Device name changed to " + deviceName);
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("ControllerFragment", "Can't change device name");
                }
            });
        } catch (NoSuchMethodException e) {
            Log.d("ControllerFragment", "Can't change device name - NoSuchMethod");
        } catch (IllegalAccessException | InvocationTargetException e) {
            Log.d("ControllerFragment", "Can't change device name");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDeviceName();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

        recordingMark = rootView.findViewById(R.id.recording_mark);
        cameraSynchronizedText = rootView.findViewById(R.id.camera_synchronized);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        cameraPreview = new CameraPreview(getContext(), camera, new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Log.d("CameraFragment", "Surface created, preparing MediaRecorder");
                preparedMediaRecorder = prepareMediaRecorder();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

            }
        });
        FrameLayout previewLayout = rootView.findViewById(R.id.camera_preview);
        previewLayout.addView(cameraPreview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_camera, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.toggle_discovery) {
            if (isDiscovering) {
                item.setIcon(R.drawable.ic_refresh);
                stopDiscovery(new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("CameraFragment", "Stopped discovery");
                        isDiscovering = false;
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d("CameraFragment", "Failed to stop discovery");
                    }
                });
            } else {
                item.setIcon(R.drawable.ic_stop);
                startDiscovery(new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("CameraFragment", "Started discovery");
                        isDiscovering = true;
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d("CameraFragment", "Failed to start discovery");
                    }
                });
            }
        } else if (id == R.id.connect) {
            for (WifiP2pDevice device : peers) {
                if (device.deviceName.contains("CONTROLLER")) {
                    connectToPeer(device.deviceAddress);
                    break;
                }
            }
        }
        return true;
    }

    private void connect(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pConfig config) throws SecurityException {
        final String address = config.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("CameraFragment", "Connected to " + address);
                menu.findItem(R.id.connect).setIcon(R.drawable.ic_check);
            }

            @Override
            public void onFailure(int reason) {
                Log.d("CameraFragment", "Failed to connect to " + address);
            }
        });
    }

    public void connectToPeer(final String address) {
        try {
            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = address;
            connect(manager, channel, config);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void reactOnMessage(Message message) {
        byte[] buffer = (byte[]) message.obj;
        String st = new String(buffer, 0, message.arg1);
        String[] messages = st.split(";");
        for (String temp : messages) {
            Log.d("CameraFragment", "Got message " + temp + " at " + System.currentTimeMillis());
            String[] split = temp.split("\\|");
            switch (split[0]) {
                case "STRT":
                    // получена команда на старт записи
                    long time = Long.parseLong(split[1]) + timeDiff;
                    waitMainThread(time - System.currentTimeMillis());
                    if (preparedMediaRecorder) {
                        camera.unlock();
                        mediaRecorder.start();
                        Log.d("CameraFragment", "Recording video, started at " + System.currentTimeMillis());
                        recordingMark.setVisibility(View.VISIBLE);
                        videoRecorded = true;
                    } else {
                        Log.d("CameraFragment", "MediaRecorder isn't ready, can't start");
                    }
                    break;
                case "STOP":
                    // получена команда на остановку записи
                    long time2 = Long.parseLong(split[1]) + timeDiff;
                    waitMainThread(time2 - System.currentTimeMillis());
                    mediaRecorder.stop();
                    camera.lock();
                    Log.d("CameraFragment", "Stopped recording video, resetting MediaRecorder");
                    preparedMediaRecorder = prepareMediaRecorder();
                    recordingMark.setVisibility(View.INVISIBLE);
                    break;
                case "UPLD":
                    // получена команда на загрузку на диск
                    String id = split[1];
                    uploader.upload(prevSavePath, id, null);
                    break;
                case "SYNC":
                    if (split[1].equals("0")) {
                        // первый этап синхронизации, отправляем серверу ответное сообщение
                        firstSync = Long.parseLong(split[2]);
                        String msg = "SYNC|" + mac;
                        client.write(msg);
                    } else if (split[1].substring(3).equals(mac.substring(3))) {
                        // второй этап синхронизации, узнаем задержку
                        long ping = (Long.parseLong(split[2]) - firstSync) / 2;
                        // узнаем разницу во времени
                        timeDiff = (System.currentTimeMillis() - ping) - firstSync;
                        Log.d("CameraFragment", "Ping: " + ping);
                        Log.d("CameraFragment", "Time difference: " + timeDiff);
                        cameraSynchronizedText.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        camera = getCameraInstance();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (camera != null) {
            camera.release();
            camera = null;
            Log.d("CameraFragment", "Camera released");
        }
        releaseMediaRecorder();
    }

    private void waitMainThread(long timeout) {
        if (timeout < 0) return;
        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait(timeout);
            } catch (InterruptedException e) {
                Log.d("CameraFragment", "Failed to wait");
            }
        }
    }

    private boolean prepareMediaRecorder() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        } else {
            mediaRecorder.reset();
        }
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        prevSavePath = nextSavePath;
        getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        nextSavePath = getOutputMediaFile();
        if (nextSavePath == null) {
            releaseMediaRecorder();
            return false;
        }
        Log.d("CameraFragment", "New file will be saved at: " + nextSavePath.toString());
        mediaRecorder.setOutputFile(nextSavePath.toString());
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
        Camera.Size maxSize = camera.getParameters().getSupportedPreviewSizes().get(0);
        Log.d("CameraFragment", "Max video size is " + maxSize.height + "*" + maxSize.width);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("CameraFragment", "IllegalStateException preparing MediaRecorder");
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("CameraFragment", "IOException preparing MediaRecorder");
            releaseMediaRecorder();
            return false;
        }
        Log.d("CameraFragment", "MediaRecorder prepared");
        videoRecorded = false;
        return true;
    }

    private void releaseMediaRecorder() {
        if (nextSavePath != null && !videoRecorded) {
            if (nextSavePath.delete()) {
                Log.d("CameraFragment", "Video wasn't recorded, deleted temp file");
            } else {
                Log.d("CameraFragment", "Video wasn't recorded, failed to delete temp file");
            }
        }
        videoRecorded = false;
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d("CameraFragment", "MediaRecorder released");
        }
    }

    @Override
    protected void reactOnPeers() {
        for (WifiP2pDevice device : peers) {
            if (device.deviceName.contains("CONTROLLER")) {
                menu.findItem(R.id.connect).setVisible(true);
                return;
            }
        }
        menu.findItem(R.id.connect).setVisible(false);
    }
}
