package ru.synccamera;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ControllerFragment extends P2PFragment implements View.OnClickListener {

    private RecyclerView list;
    private ListRVAdapter adapter;

    public ControllerFragment() {
        super(R.layout.fragment_controller);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controller, container, false);
        rootView.findViewById(R.id.peer_search).setOnClickListener(this);
        rootView.findViewById(R.id.send_command).setOnClickListener(this);
        list = rootView.findViewById(R.id.peer_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListRVAdapter(new ArrayList<PeerListItem>(), this);
        list.setAdapter(adapter);
        return rootView;
    }

    @Override
    protected void reactOnPeers() {
        updateList();
    }

    public void connectToPeer(final String address) {
        try {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = address;
            config.groupOwnerIntent = 15;
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.d("SyncCamera", "Connected to " + address);
                    updateList();
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("SyncCamera", "Failed to connect to " + address);
                    updateList();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateList() {
        List<PeerListItem> listItems = new ArrayList<>();
        for (WifiP2pDevice device : peers) {
            listItems.add(new PeerListItem(device.deviceName, statusToString(device.status), device.deviceAddress));
        }
        adapter.setList(listItems);
        adapter.notifyDataSetChanged();
    }

    private String statusToString(int status) {
        switch (status) {
            case 0:
                return getString(R.string.device_connected);
            case 1:
                return getString(R.string.device_invite);
            case 2:
                return getString(R.string.device_error);
            case 3:
                return getString(R.string.device_available);
        }
        return "";
    }

    @Override
    public void receive(WifiP2pDevice parcelableExtra) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.peer_search:
                startDiscovery(new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d("SyncCamera", "Started discovery");
                        updateList();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d("SyncCamera", "Failed to start discovery");
                        updateList();
                    }
                });
                break;
            case R.id.send_command:
                server.write("START".getBytes());
        }
    }
}
