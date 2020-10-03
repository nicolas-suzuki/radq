package com.aden.radq;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
                setContentView(R.layout.main_activity);
                ImageButton bttnCamera = findViewById(R.id.bttnCamera);
                bttnCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCameraActivity();
                    }
                });

                ImageButton bttnAlarms = findViewById(R.id.bttnAlarms);
                bttnAlarms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openAlarmsActivity();
                    }
                });

                ImageButton bttnNotifications = findViewById(R.id.bttnNotifications);
                bttnNotifications.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNotificationsActivity();
                    }
                });

                ImageButton bttnSettings = findViewById(R.id.bttnSettings);
                bttnSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSettingsActivity();
                    }
                });

    }

    public void openCameraActivity(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void openAlarmsActivity(){
        Intent intent = new Intent(this, AlarmsActivity.class);
        startActivity(intent);
    }

    public void openNotificationsActivity(){
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void checkPermission(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        },PERMISSIONS_CODE);
    }

    private void downloadNecessaryFiles() {
        if (checkDownloadedFiles()) { //check if files already there
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            assert downloadManager != null;

            Uri uri = Uri.parse("https://drive.google.com/uc?export=download&id=1QTWqtQSASSe8AIugP6tb2480Ro7Gt2yN");
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.downloading_necessary_files));
            request.setDescription(getString(R.string.downloading_WEIGHT_File));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.parse("file://" + getExternalFilesDir(null) + "/yolov3-tiny.weights"));
            downloadManager.enqueue(request);

            uri = Uri.parse("https://drive.google.com/uc?export=download&id=1Y0CX4-Z4ZrteVkuzj2B8MU6WT65qIrw0");
            request = new DownloadManager.Request(uri);
            request.setTitle(getString(R.string.downloading_necessary_files));
            request.setDescription(getString(R.string.downloading_CFG_File));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.parse("file://" + getExternalFilesDir(null) + "/yolov3-tiny.cfg"));
            downloadManager.enqueue(request);
        }
    }

    private boolean checkDownloadedFiles() {
        String path = Objects.requireNonNull(getExternalFilesDir(null)).toString() + "/dnns";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        Log.d("Files", "Size: " + files.length);

        for (File file : files) {
            Log.d("Files", "FileName: " + file.getName());
        }
        return true;
    }
}
