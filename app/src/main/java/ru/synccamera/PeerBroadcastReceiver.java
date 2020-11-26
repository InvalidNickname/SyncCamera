package ru.synccamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import androidx.appcompat.app.AppCompatActivity;

public class PeerBroadcastReceiver extends BroadcastReceiver {

    AppCompatActivity activity;
    WifiP2pManager manager;
    WifiP2pManager.PeerListListener listener;
    WifiP2pManager.Channel channel;

    public PeerBroadcastReceiver(AppCompatActivity activity, WifiP2pManager.PeerListListener listener, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.activity = activity;
        this.manager = manager;
        this.listener = listener;
        this.channel = channel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

            } else {

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                try {
                    manager.requestPeers(channel, listener);
                } catch (SecurityException ignored) {

                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            P2PFragment fragment = (P2PFragment) activity.getSupportFragmentManager().findFragmentById(R.id.fragment_main);
            fragment.receive((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}
