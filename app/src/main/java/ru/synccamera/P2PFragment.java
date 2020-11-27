package ru.synccamera;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class P2PFragment extends Fragment {

    protected final IntentFilter intentFilter = new IntentFilter();
    protected List<WifiP2pDevice> peers = new ArrayList<>();
    protected WifiP2pManager.Channel channel;
    protected WifiP2pManager manager;
    protected PeerBroadcastReceiver receiver;
    protected int id;
    protected boolean isDiscovering = false;
    protected Server server;
    protected Client client;
    private WifiManager.WifiLock wifiLock;
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
            if (!refreshedPeers.equals(peers)) {
                Log.d("SyncCamera", "New peer list available:");
                for (WifiP2pDevice peer : refreshedPeers) {
                    if (!peers.contains(peer)) {
                        Log.d("SyncCamera", "\t+ " + peer.deviceName + " | " + peer.deviceAddress);
                    }
                }
                for (WifiP2pDevice peer : peers) {
                    if (!refreshedPeers.contains(peer)) {
                        Log.d("SyncCamera", "\t- " + peer.deviceName + " | " + peer.deviceAddress);
                    }
                }
                peers.clear();
                peers.addAll(refreshedPeers);
                reactOnPeers();
            }
        }
    };

    private WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.i("SyncCamera", "Connected as a host " + groupOwnerAddress);
                if (server == null) {
                    server = new Server();
                }
                server.newConnection();
            } else {
                Log.i("SyncCamera", "Connected to " + groupOwnerAddress + " as a client");
                client = new Client(groupOwnerAddress, new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        reactOnMessage(message);
                        return true;
                    }
                }));
                client.start();
            }
        }
    };

    public P2PFragment(int layoutId) {
        id = layoutId;
    }

    protected void reactOnMessage(Message message) {

    }

    protected void reactOnPeers() {

    }

    protected void startDiscovery(WifiP2pManager.ActionListener listener) {
        try {
            manager.discoverPeers(channel, listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    protected void stopDiscovery(WifiP2pManager.ActionListener listener) {
        try {
            manager.stopPeerDiscovery(channel, listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(id, container, false);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(getContext(), getMainLooper(), null);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "SyncCamera");
        wifiLock.acquire();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && manager != null && channel != null) {
                        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("SyncCamera", "Cancelling connections");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("SyncCamera", "Failed to cancel connections");
                            }
                        });
                    }
                }
            });
        } catch (SecurityException e) {
            Log.d("SyncCamera","Failed to cancel connections");
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new PeerBroadcastReceiver((AppCompatActivity) getContext(), peerListListener, manager, channel, connectionInfoListener);
        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

}
