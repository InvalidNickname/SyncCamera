package ru.synccamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder surfaceHolder;
    private final Camera camera;

    public CameraPreview(Context context, Camera camera, SurfaceHolder.Callback callback) {
        super(context);
        this.camera = camera;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.addCallback(callback);
    }

    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            Log.d("SyncCamera", "Camera preview started");
        } catch (IOException e) {
            Log.d("SyncCamera", "Error setting camera preview");
        }
    }

    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {

    }
}
