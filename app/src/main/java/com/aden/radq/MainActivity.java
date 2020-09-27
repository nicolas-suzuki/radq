package com.aden.radq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
}
