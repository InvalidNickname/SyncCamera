package ru.synccamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListRVAdapter extends RecyclerView.Adapter<ListRVAdapter.CardViewHolder> {

    private final ControllerFragment fragment;
    private List<PeerListItem> peers;

    public ListRVAdapter(List<PeerListItem> peers, ControllerFragment fragment) {
        this.peers = peers;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_peer, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, final int i) {
        cardViewHolder.name.setText(peers.get(i).getName());
        int status = peers.get(i).getStatus();
        Context context = cardViewHolder.main.getContext();
        cardViewHolder.status.setText(statusToString(context, status));
        switch (status) {
            case 0:
                cardViewHolder.main.setBackgroundColor(context.getColor(R.color.green));
                break;
            case 4:
                cardViewHolder.main.setBackgroundColor(context.getColor(R.color.red));
                break;
            default:
                cardViewHolder.main.setBackground(null);
                break;
        }
        cardViewHolder.mac.setText(peers.get(i).getAddress());
    }

    private String statusToString(Context context, int status) {
        switch (status) {
            case 0:
                return context.getString(R.string.device_connected);
            case 1:
                return context.getString(R.string.device_invite);
            case 2:
                return context.getString(R.string.device_error);
            case 3:
                return context.getString(R.string.device_available);
            case 4:
                return context.getString(R.string.device_connection_lost_while_recording);
        }
        return "";
    }

    public List<PeerListItem> getList() {
        return peers;
    }

    public void setList(List<PeerListItem> list) {
        peers = list;
    }

    @Override
    public int getItemCount() {
        return peers.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView status;
        final ConstraintLayout main;
        final TextView mac;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.card);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            mac = itemView.findViewById(R.id.mac);
        }
    }
}
