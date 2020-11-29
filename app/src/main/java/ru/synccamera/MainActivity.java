package ru.synccamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_main, SelectorFragment.class, null)
                .commit();
        String[] permissions = new String[]{
                "android.permission.ACCESS_FINE_LOCATION", "android.permission.CAMERA",
                "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO",
                "android.permission.READ_EXTERNAL_STORAGE"};
        if (!hasPermissions(this, permissions)) {
            requestPermissions(permissions, 1);
        }
    }
}