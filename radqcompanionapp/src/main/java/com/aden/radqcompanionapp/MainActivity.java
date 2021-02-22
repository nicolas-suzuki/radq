package com.aden.radqcompanionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aden.radqcompanionapp.utils.FirebaseConnector;
import com.aden.radqcompanionapp.utils.SettingsStorage;
import com.aden.radqcompanionapp.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        //Load settings
        SettingsStorage settingsStorage = new SettingsStorage(MainActivity.this);

        if(settingsStorage.getIdentifierKey() != null) {
            if ((settingsStorage.getIdentifierKey().isEmpty()) && (firebaseAuth.getCurrentUser() != null)) {
                firebaseAuth.signOut();
            }
            if ((firebaseAuth.getCurrentUser() == null) && (!settingsStorage.getIdentifierKey().isEmpty())) {
                settingsStorage.setIdentifierKey("");
            }
        }

        setContentView(R.layout.main_activity);

        ImageButton ibNotifications = findViewById(R.id.ibNotifications);
        ibNotifications.setOnClickListener(v -> openNotificationsActivity());

        ImageButton ibSettings = findViewById(R.id.ibLogin);
        ibSettings.setOnClickListener(v -> openLoginActivity());
    }

    private void openNotificationsActivity(){
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    private void openLoginActivity(){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //Dialog box to warn the user about not defining a contact in the application settings
    private void alertDialogBox(String title, String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(getString(R.string.positive_button), null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}
