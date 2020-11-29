package ru.synccamera;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;

public class GDriveUploader {

    private Drive service;

    public void auth(Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        service = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName("SyncCamera").build();
    }

    public String createFolder(String name) {
        Log.d("SyncCamera", "Creating folder " + name);
        if (service != null) {
            try {
                File file = new File();
                file.setName(name);
                file.setMimeType("application/vnd.google-apps.folder");
                file.setParents(Collections.singletonList("root"));
                Loader loader = new Loader(file);
                loader.start();
                loader.join();
                return loader.getID();
            } catch (InterruptedException e) {
                Log.d("SyncCamera", "Failed to create folder");
            }
        } else {
            Log.d("SyncCamera", "Failed to create folder without logging in");
        }
        return "";
    }

    public void upload(java.io.File file, String folderId) {
        if (service != null) {
            try {
                File fileMeta = new File();
                fileMeta.setName(file.getName());
                fileMeta.setMimeType("video/mp4");
                fileMeta.setParents(Collections.singletonList(folderId));
                FileContent content = new FileContent("video/mp4", file);
                Loader loader = new Loader(fileMeta, content);
                loader.start();
                loader.join();
            } catch (InterruptedException e) {
                Log.d("SyncCamera", "Failed to upload file");
            }
        } else {
            Log.d("SyncCamera", "Failed to upload file without logging in");
        }
    }

    private class Loader extends Thread {
        private final File meta;
        private volatile String id;
        private FileContent content;

        public Loader(File meta) {
            this.meta = meta;
        }

        public Loader(File meta, FileContent content) {
            this.meta = meta;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                File newFile;
                if (content == null) {
                    Log.d("SyncCamera", "1");
                    newFile = service.files().create(meta).execute();
                    Log.d("SyncCamera", "2");
                } else {
                    newFile = service.files().create(meta, content).execute();
                }
                id = newFile.getId();
            } catch (IOException e) {
                Log.d("SyncCamera", "Failed to create file/folder");
            }
        }

        public String getID() {
            return id;
        }
    }
}
