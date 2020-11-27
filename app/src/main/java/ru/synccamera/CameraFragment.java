package ru.synccamera;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CameraFragment extends P2PFragment {

    public CameraFragment() {
        super(R.layout.fragment_camera);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_camera, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_discovery:
                if (isDiscovering) {
                    item.setIcon(R.drawable.ic_refresh);
                    stopDiscovery(new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("SyncCamera", "Stopped discovery");
                            isDiscovering = false;
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Log.d("SyncCamera", "Failed to stop discovery");
                        }
                    });
                } else {
                    item.setIcon(R.drawable.ic_stop);
                    startDiscovery(new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("SyncCamera", "Started discovery");
                            isDiscovering = true;
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Log.d("SyncCamera", "Failed to start discovery");
                        }
                    });
                }
                break;
        }
        return true;
    }

    @Override
    protected void reactOnMessage(Message message) {
        byte[] buffer = (byte[]) message.obj;
        String temp = new String(buffer, 0, message.arg1);
        Log.d("SyncCamera", "Got message " + temp + " at " + System.currentTimeMillis());
    }

}
