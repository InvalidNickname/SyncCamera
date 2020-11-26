package ru.synccamera;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CameraFragment extends P2PFragment {

    public CameraFragment() {
        super(R.layout.fragment_camera);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        return rootView;
    }

    @Override
    public void receive(WifiP2pDevice parcelableExtra) {

    }

}
