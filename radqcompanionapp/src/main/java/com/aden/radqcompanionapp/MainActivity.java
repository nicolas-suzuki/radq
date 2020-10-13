package com.aden.radqcompanionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aden.radqcompanionapp.adapter.FirebaseConnector;
import com.aden.radqcompanionapp.helper.Settings;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ImageButton ibNotifications = findViewById(R.id.ibNotifications);
        ImageButton ibSettings = findViewById(R.id.ibLogin);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();
        Settings settings = new Settings(MainActivity.this);


        if(settings.getIdentifier() != null){
            if((settings.getIdentifier().isEmpty()) && (firebaseAuth.getCurrentUser() != null)){
                Log.d(TAG, "Logging out, since settings is empty");
                firebaseAuth.signOut();
            }
            if((firebaseAuth.getCurrentUser() == null) && (!settings.getIdentifier().isEmpty())){
                Log.d(TAG, "Setting settings as empty, since logged out");
                settings.setIdentifier("");
            }
        }

        ibNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNotifications();
            }
        });

        ibSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
    }

    private void openNotifications(){
        Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }

    private void openSettings(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
