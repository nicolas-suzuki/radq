package com.aden.radq;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aden.radq.model.Contact;
import com.aden.radq.utils.FirebaseConnector;
import com.aden.radq.utils.SettingsStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_CODE = 1;

    boolean isLoadingContacts = true;
    ArrayList<String> myContacts;

    private SettingsStorage settingsStorage;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListenerMyContacts;

    @Override
    protected final void onStop() {
        super.onStop();
        if(valueEventListenerMyContacts != null) {
            databaseReference.removeEventListener(valueEventListenerMyContacts);
        }
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        //Load settings
        settingsStorage = new SettingsStorage(MainActivity.this);

        myContacts = new ArrayList<>();

        settingsStorage.getIdentifierKey();
        if ((settingsStorage.getIdentifierKey().isEmpty()) && (firebaseAuth.getCurrentUser() != null)) {
            firebaseAuth.signOut();
        }
        if ((firebaseAuth.getCurrentUser() == null) && (!settingsStorage.getIdentifierKey().isEmpty())) {
            settingsStorage.setIdentifierKey("");
        }

        if(firebaseAuth.getCurrentUser() != null){
            initiateFirebaseDatabase();
        }

        setContentView(R.layout.main_activity);

        ImageButton bttnCamera = findViewById(R.id.bttnCamera);
        bttnCamera.setOnClickListener(v -> openCameraActivity());

        ImageButton bttnNotifications = findViewById(R.id.bttnNotifications);
        bttnNotifications.setOnClickListener(v -> openNotificationsActivity());

        ImageButton bttnSettings = findViewById(R.id.bttnSettings);
        bttnSettings.setOnClickListener(v -> openSettingsActivity());
    }

    public final void openCameraActivity(){
        //Get the saved preferences and check if there's a contact registered
        //if not, it won't start the CameraActivity and will show up a message
        if(firebaseAuth.getCurrentUser() != null){
            if(valueEventListenerMyContacts == null){
                initiateFirebaseDatabase();
            } else {
                databaseReference.addValueEventListener(valueEventListenerMyContacts);
            }
            if (isLoadingContacts) {
                alertDialogBox(getString(R.string.dialog_title_loading_contacts), getString(R.string.dialog_message_loading_contacts));
            } else {
                if (myContacts.isEmpty()) {
                    alertDialogBox(getString(R.string.contact_alert_dialog_title), getString(R.string.contact_alert_dialog_message));
                } else {
                    Intent intent = new Intent(MainActivity.this, StartRadqActivity.class);
                    startActivity(intent);
                }
            }
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    public final void openNotificationsActivity(){
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }
    }

    public final void openSettingsActivity(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public final void checkPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        },PERMISSIONS_CODE);
    }

    private void initiateFirebaseDatabase(){
        databaseReference = FirebaseConnector.getFirebase().
                child("contacts").
                child(settingsStorage.getIdentifierKey());
        valueEventListenerMyContacts = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myContacts.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Contact contact = data.getValue(Contact.class);
                    myContacts.add(Objects.requireNonNull(contact).getName());
                }
                isLoadingContacts = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isLoadingContacts = true;
            }
        };
        databaseReference.addValueEventListener(valueEventListenerMyContacts);
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
