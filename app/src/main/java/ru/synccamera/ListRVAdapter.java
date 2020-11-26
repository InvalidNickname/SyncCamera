package ru.synccamera;

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
        cardViewHolder.status.setText(peers.get(i).getStatus());
        cardViewHolder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.connectToPeer(peers.get(i).getAddress());
            }
        });
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

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.card);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
        }
    }
}
