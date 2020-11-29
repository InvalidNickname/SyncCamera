package ru.synccamera;

import android.content.Context;
import android.os.AsyncTask;
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
        if (account != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(Scopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            service = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                    .setApplicationName("SyncCamera").build();
        }
    }

    public void createFolder(String name, OnUploadCompleted callback) {
        Log.d("SyncCamera", "Creating folder " + name);
        if (service != null) {
            File file = new File();
            file.setName(name);
            file.setMimeType("application/vnd.google-apps.folder");
            file.setParents(Collections.singletonList("root"));
            Loader loader = new Loader(file, callback);
            loader.execute();
        } else {
            Log.d("SyncCamera", "Failed to create folder without logging in");
        }
    }

    public void upload(java.io.File file, String folderId, OnUploadCompleted callback) {
        if (service != null) {
            File fileMeta = new File();
            fileMeta.setName(file.getName());
            fileMeta.setMimeType("video/mp4");
            fileMeta.setParents(Collections.singletonList(folderId));
            FileContent content = new FileContent("video/mp4", file);
            Loader loader = new Loader(fileMeta, content, callback);
            loader.execute();
        } else {
            Log.d("SyncCamera", "Failed to upload file without logging in");
        }
    }

    interface OnUploadCompleted {
        void onComplete(String id);
    }

    private class Loader extends AsyncTask<Void, Void, Void> {
        private final File meta;
        private String id;
        private FileContent content;
        private OnUploadCompleted callback;

        public Loader(File meta, OnUploadCompleted callback) {
            this.meta = meta;
            this.callback = callback;
        }

        public Loader(File meta, FileContent content, OnUploadCompleted callback) {
            this.meta = meta;
            this.content = content;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                File newFile;
                if (content == null) {
                    newFile = service.files().create(meta).execute();
                    Log.d("SyncCamera", "Folder created");
                } else {
                    newFile = service.files().create(meta, content).execute();
                    Log.d("SyncCamera", "File created");
                }
                id = newFile.getId();
            } catch (IOException e) {
                Log.d("SyncCamera", "Failed to create file/folder");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (callback != null) {
                callback.onComplete(id);
            }
        }
    }
}
