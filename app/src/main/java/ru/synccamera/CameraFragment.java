package ru.synccamera;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CameraFragment extends P2PFragment {

    public CameraFragment() {
        super(R.layout.fragment_camera);
        role = "CAMERA";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        return rootView;
    }

    @Override
    protected void reactOnMessage(Message message) {
        byte[] buffer = (byte[]) message.obj;
        String temp = new String(buffer, 0, message.arg1);
        Log.d("SyncCamera", temp);
    }

    @Override
    public void onResume() {
        super.onResume();
        startDiscovery(new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d("SyncCamera", "Started discovery");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("SyncCamera", "Failed to start discovery");
            }
        });
    }

    @Override
    public void receive(WifiP2pDevice parcelableExtra) {

    }

}
