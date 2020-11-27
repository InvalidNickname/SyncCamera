package ru.synccamera;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
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
        rootView.findViewById(R.id.send_command).setOnClickListener(this);
        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        list = rootView.findViewById(R.id.peer_list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListRVAdapter(new ArrayList<PeerListItem>(), this);
        list.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_controller, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toggle_discovery:
                if (isDiscovering) {
                    item.setIcon(R.drawable.ic_refresh);
                    stopDiscovery(new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("SyncCamera", "Stopped discovery");
                            List<PeerListItem> items = new ArrayList<>();
                            for (WifiP2pDevice device : peers) {
                                if (device.status == 0) {
                                    items.add(new PeerListItem(device.deviceName, device.status, device.deviceAddress));
                                }
                            }
                            updateList(items);
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
                            updateList();
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
            listItems.add(new PeerListItem(device.deviceName, device.status, device.deviceAddress));
        }
        adapter.setList(listItems);
        adapter.notifyDataSetChanged();
    }

    private void updateList(List<PeerListItem> items) {
        adapter.setList(items);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_command:
                if (server != null) {
                    server.write("START".getBytes());
                } else {
                    Log.d("SyncCamera", "Server not started");
                }
        }
    }
}
