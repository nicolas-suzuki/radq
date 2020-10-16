package com.aden.radq;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.aden.radq.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSIONS_CODE = 1;

    private ArrayList<String> myContacts;

    private FirebaseAuth firebaseAuth;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        //Load settings
        settings = new Settings(MainActivity.this);
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());

        myContacts = new ArrayList<>();

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

        ImageButton bttnCamera = findViewById(R.id.bttnCamera);
        bttnCamera.setOnClickListener(v -> openCameraActivity());

        ImageButton bttnAlarms = findViewById(R.id.bttnAlarms);
        bttnAlarms.setOnClickListener(v -> openAlarmsActivity());

        ImageButton bttnNotifications = findViewById(R.id.bttnNotifications);
        bttnNotifications.setOnClickListener(v -> openNotificationsActivity());

        ImageButton bttnSettings = findViewById(R.id.bttnSettings);
        bttnSettings.setOnClickListener(v -> openSettingsActivity());
    }

    public void openCameraActivity(){
        //Get the saved preferences and check if there's a contact registered
        //if not, it won't start the CameraActivity and will show up a message

        if(firebaseAuth.getCurrentUser() != null){
            DatabaseReference databaseReference = FirebaseConnector.getFirebase().
                    child("contacts").
                    child(settings.getIdentifierKey());
            ValueEventListener valueEventListenerMyContacts = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myContacts.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Contact contact = data.getValue(Contact.class);
                        myContacts.add(contact.getName());
                    }
                    if (myContacts.isEmpty()) {
                        alertDialogBox(getString(R.string.contact_alert_dialog_title), getString(R.string.contact_alert_dialog_message));
                    } else {
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            databaseReference.addValueEventListener(valueEventListenerMyContacts);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    public void openAlarmsActivity(){
        Intent intent = new Intent(MainActivity.this, AlarmsActivity.class);
        startActivity(intent);
    }

    public void openNotificationsActivity(){
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void checkPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        },PERMISSIONS_CODE);
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
