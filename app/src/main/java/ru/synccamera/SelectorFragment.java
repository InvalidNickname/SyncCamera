package ru.synccamera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class SelectorFragment extends Fragment implements View.OnClickListener {

    public SelectorFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selector, container, false);
        rootView.findViewById(R.id.set_controller).setOnClickListener(this);
        rootView.findViewById(R.id.set_camera).setOnClickListener(this);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_controller:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_main, ControllerFragment.class, null)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.set_camera:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_main, CameraFragment.class, null)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}
