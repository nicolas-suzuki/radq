package com.aden.radqcompanionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        //Load settings
        Settings settings = new Settings(MainActivity.this);
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());

        if(settings.getIdentifierKey() != null) {
            if ((settings.getIdentifierKey().isEmpty()) && (firebaseAuth.getCurrentUser() != null)) {
                Log.d(TAG, "Logging out, since settings is empty");
                firebaseAuth.signOut();
            }
            if ((firebaseAuth.getCurrentUser() == null) && (!settings.getIdentifierKey().isEmpty())) {
                Log.d(TAG, "Setting settings as empty, since logged out");
                settings.setIdentifierKey("");
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
