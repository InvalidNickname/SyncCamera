package ru.synccamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private boolean changePref = true;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);
        PreferenceManager.setDefaultValues(context, R.xml.prefs, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        // установка текста версии
        String versionName = "unknown";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("SyncCamera", "Unable to get version");
        }
        Preference versionPref = findPreference("version");
        if (versionPref != null) {
            versionPref.setTitle(String.format(getResources().getString(R.string.pref_version), versionName));
        }
        // запрет на overscroll, без него выглядит лучше
        getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("DefaultLocale")
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "from_temp":
                // изменение гугл адреса отправителя
                if (changePref) {
                    changePref = false;
                    String from = sharedPreferences.getString("from_temp", "");
                    // пустая строка = не менять
                    if (from.isEmpty()) break;
                    sharedPreferences.edit()
                            .putString("from", from)
                            .putString("from_temp", "")
                            .apply();
                } else {
                    changePref = true;
                }
                break;
            case "pass_temp":
                // изменение пароля отправителя
                if (changePref) {
                    changePref = false;
                    String pass = sharedPreferences.getString("pass_temp", "");
                    // пустая строка = не менять
                    if (pass.isEmpty()) break;
                    sharedPreferences.edit()
                            .putString("pass", pass)
                            .putString("pass_temp", "")
                            .apply();
                } else {
                    changePref = true;
                }
                break;
        }
    }
}
