package ru.synccamera;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
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
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ControllerFragment extends P2PFragment implements View.OnClickListener {

    private ListRVAdapter adapter;
    private boolean isRecording = false;
    private boolean wasRecording = false;
    private List<PeerListItem> currentActive = new ArrayList<>();
    private Button recordButton;
    private boolean groupCreated = false;

    public ControllerFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_controller, container, false);

        // кнопка начала/окончания записи
        recordButton = rootView.findViewById(R.id.send_command);
        recordButton.setOnClickListener(this);

        // кнопка загрузки на диск
        rootView.findViewById(R.id.upload).setOnClickListener(this);

        // тулбар
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // список устройств
        RecyclerView list = rootView.findViewById(R.id.peer_list);
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
            // кнопка смены режима поиска
            case R.id.toggle_discovery:
                if (isDiscovering) {
                    // остановка поиска, меняем иконку кнопки поиска
                    item.setIcon(R.drawable.ic_refresh);
                    stopDiscovery(new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("SyncCamera", "Stopped discovery");
                            // создаем новый список из всех подключенных устройств
                            List<PeerListItem> items = new ArrayList<>();
                            for (WifiP2pDevice device : peers) {
                                if (device.status == 0) {
                                    items.add(new PeerListItem(device.deviceName, device.status, device.deviceAddress));
                                }
                            }
                            updateList(items);
                            isDiscovering = false;
                            if (server != null) {
                                String message = "SYNC|0|" + System.currentTimeMillis();
                                server.write(message);
                            }
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Log.d("SyncCamera", "Failed to stop discovery");
                        }
                    });
                } else {
                    // начало поиска, меняем иконку кнопки поиска
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
    protected void reactOnMessage(Message message) {
        byte[] buffer = (byte[]) message.obj;
        String st = new String(buffer, 0, message.arg1);
        String[] messages = st.split(";");
        for (String temp : messages) {
            Log.d("SyncCamera", "Got message " + temp + " at " + System.currentTimeMillis());
            String[] split = temp.split("\\|");
            switch (split[0]) {
                case "SYNC":
                    String msg = temp + "|" + System.currentTimeMillis();
                    server.write(msg);
                    break;
            }
        }
    }

    @Override
    protected void reactOnPeers() {
        updateList();
    }

    private void connect(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiP2pConfig config) throws SecurityException {
        final String address = config.deviceAddress;
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
    }

    public void connectToPeer(final String address) {
        try {
            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = address;
            config.groupOwnerIntent = 15;
            if (!groupCreated) {
                manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("SyncCamera", "Group created");
                        groupCreated = true;
                        connect(manager, channel, config);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("SyncCamera", "Failed to create a group");
                    }
                });
            } else {
                connect(manager, channel, config);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateList() {
        List<PeerListItem> listItems = new ArrayList<>();
        for (WifiP2pDevice device : peers) {
            listItems.add(new PeerListItem(device.deviceName, device.status, device.deviceAddress));
        }
        if (isRecording) {
            // проверяем, не отвалилось ли какое-либо устройство во время записи
            for (int i = 0; i < currentActive.size(); ++i) {
                if (!listItems.contains(currentActive.get(i))) {
                    currentActive.get(i).setStatus(4);
                }
            }
            listItems = currentActive;
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
                    if (isDiscovering) {
                        Log.d("SyncCamera", "Refused to send command while discovering");
                        Toast.makeText(getContext(), R.string.refused_to_send_command_while_discovering, Toast.LENGTH_LONG).show();
                    } else if (server.getNumberOfConnections() == 0) {
                        Log.d("SyncCamera", "Refused to send command without active connections");
                        Toast.makeText(getContext(), R.string.refused_to_send_command_no_active_devices, Toast.LENGTH_LONG).show();
                    } else {
                        long executeTime = System.currentTimeMillis() + 500;
                        if (isRecording) {
                            // посылаем команду на остановку записи
                            String message = "STOP|" + executeTime;
                            server.write(message);
                            isRecording = false;
                            recordButton.setText(R.string.start_recording);
                        } else {
                            // посылаем команду на начало записи
                            String message = "STRT|" + executeTime;
                            server.write(message);
                            isRecording = true;
                            wasRecording = true;
                            currentActive = new ArrayList<>();
                            for (WifiP2pDevice device : peers) {
                                if (device.status == 0) {
                                    currentActive.add(new PeerListItem(device.deviceName, device.status, device.deviceAddress));
                                }
                            }
                            recordButton.setText(R.string.stop_recording);
                        }
                    }
                } else {
                    Log.d("SyncCamera", "Server not started");
                    Toast.makeText(getContext(), R.string.server_not_started, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.upload:
                if (isRecording) {
                    Log.d("SyncCamera", "Refused to send upload command while recording");
                    Toast.makeText(getContext(), R.string.refused_to_send_upload_command_while_recording, Toast.LENGTH_LONG).show();
                } else if (!wasRecording) {
                    Log.d("SyncCamera", "Refused to send upload command - no videos");
                    Toast.makeText(getContext(), R.string.refused_to_send_upload_command_no_videos, Toast.LENGTH_LONG).show();
                } else {
                    // посылаем команду на отправку записи
                    String folderName = String.valueOf(System.currentTimeMillis());
                    String id = uploader.createFolder(folderName);
                    String message = "UPLD|" + id;
                    server.write(message);
                }
        }
    }
}
