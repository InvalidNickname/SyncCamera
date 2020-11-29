package ru.synccamera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

public class SelectorFragment extends Fragment implements View.OnClickListener {

    private GoogleSignInClient googleSignInClient;

    public SelectorFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selector, container, false);

        // кнопки выбора режима телефона
        rootView.findViewById(R.id.set_controller).setOnClickListener(this);
        rootView.findViewById(R.id.set_camera).setOnClickListener(this);
        // кнопка входа в аккаунт
        rootView.findViewById(R.id.sign_in).setOnClickListener(this);

        // подключение тулбара
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        // конфигурация для входа в гугл аккаунт
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        //googleSignInClient.signOut();
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
            case R.id.sign_in:
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
                if (account == null) {
                    // нет уже готового пользователя, входим
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 1);
                } else {
                    // пользователь уже вошёл
                    Toast.makeText(getContext(), getString(R.string.already_signed_in), Toast.LENGTH_LONG).show();
                    checkForGooglePermissions();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("SyncCamera", "Successfully authorized");
            checkForGooglePermissions();
        } catch (ApiException e) {
            Log.d("SyncCamera", "Failed to authorize");
        }
    }

    private void checkForGooglePermissions() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext()),
                new Scope(Scopes.DRIVE_FILE),
                new Scope(Scopes.EMAIL))) {
            GoogleSignIn.requestPermissions(
                    getActivity(), 1,
                    GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext()),
                    new Scope(Scopes.DRIVE_FILE),
                    new Scope(Scopes.EMAIL));
        } else {
            Log.d("SyncCamera", "Permission to access Drive and Email has been granted");
        }
    }
}
