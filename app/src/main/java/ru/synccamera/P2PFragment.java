package ru.synccamera;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class P2PFragment extends Fragment {

    protected final IntentFilter intentFilter = new IntentFilter();
    protected final List<WifiP2pDevice> peers = new ArrayList<>();
    private final WifiP2pManager.PeerListListener peerListListener = peerList -> {
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
    };
    protected WifiP2pManager.Channel channel;
    protected WifiP2pManager manager;
    protected PeerBroadcastReceiver receiver;
    protected boolean isDiscovering = false;
    protected Server server;
    protected Client client;
    protected Context context;
    protected String mac;
    protected String deviceName;
    protected GDriveUploader uploader;
    protected boolean shutdown = false;
    private WifiManager.WifiLock wifiLock;
    private boolean firstCall = true;
    private final WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            final int port = context.getResources().getInteger(R.integer.port);
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.i("P2PFragment", "Connected as a host " + groupOwnerAddress);
                if (firstCall) {
                    firstCall = false;
                } else {
                    if (server == null) {
                        server = new Server(port, new Handler(message -> {
                            reactOnMessage(message);
                            return true;
                        }));
                    }
                    server.newConnection();
                }
            } else {
                if (groupOwnerAddress != null) {
                    Log.i("P2PFragment", "Connected to " + groupOwnerAddress + " as a client");
                    client = new Client(port, groupOwnerAddress, new Handler(message -> {
                        reactOnMessage(message);
                        return true;
                    }));
                    client.start();
                }
            }
        }
    };

    public P2PFragment() {

    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                Log.d("P2PFragment", "MAC: " + res1.toString());
                return res1.toString();
            }
        } catch (Exception ex) {
            Log.d("P2PFragment", "Can't get MAC");
        }
        return "02:00:00:00:00:00";
    }

    private void setupGoogleDriveUploader() {
        uploader = new GDriveUploader();
        uploader.auth(getActivity().getApplicationContext());
        Log.d("P2PFragment", "Successfully authorized in Google");
    }

    protected void reactOnMessage(Message message) {

    }

    protected void reactOnPeers() {

    }

    private void discover(WifiP2pManager.ActionListener listener) throws SecurityException {
        if (!shutdown) {
            new android.os.Handler().postDelayed(() -> {
                manager.discoverPeers(channel, listener);
                discover(listener);
            }, 5000);
        }
    }

    protected void startDiscovery(WifiP2pManager.ActionListener listener) {
        try {
            manager.discoverPeers(channel, listener);
            discover(listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    protected void stopDiscovery(WifiP2pManager.ActionListener listener) {
        shutdown = true;
        try {
            manager.stopPeerDiscovery(channel, listener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, getMainLooper(), null);
        cancelConnections();
        setupGoogleDriveUploader();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "SyncCamera");
        wifiLock.acquire();

        mac = getMacAddress();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        shutdown = true;
        cancelConnections();
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    private void deletePersistentGroups() {
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    for (int netid = 0; netid < 32; netid++) {
                        method.invoke(manager, channel, netid, null);
                    }
                }
            }
            Log.d("P2PFragment", "Deleted persistent groups");
        } catch (Exception e) {
            Log.d("P2PFragment", "Failed to delete persistent groups");
        }
    }

    protected void cancelConnections() {
        try {
            manager.requestGroupInfo(channel, group -> {
                if (group != null && manager != null && channel != null) {
                    manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("P2PFragment", "Cancelling connections");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("P2PFragment", "Failed to cancel connections");
                        }
                    });
                }
            });
        } catch (SecurityException e) {
            Log.d("P2PFragment", "Failed to cancel connections");
        }
        deletePersistentGroups();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new PeerBroadcastReceiver((AppCompatActivity) getContext(), peerListListener, manager, channel, connectionInfoListener);
        context.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(receiver);
    }

}
