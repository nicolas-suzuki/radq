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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PERMISSIONS_CODE = 1;

    private DatabaseReference databaseReference;

    private ValueEventListener valueEventListenerMyContacts;

    private ArrayList<String> myContacts;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        workOnAdditionalFiles();

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

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
        Settings settings = new Settings(MainActivity.this);
        //Log.d(TAG,settings.getIdentifier());

        myContacts = new ArrayList<>();

        if(firebaseAuth.getCurrentUser() != null){
            databaseReference = FirebaseConnector.getFirebase().child("contacts").child(settings.getIdentifier());
            valueEventListenerMyContacts = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    myContacts.clear();
                    for(DataSnapshot data : snapshot.getChildren()){
                        Contact contact = data.getValue(Contact.class);
                        myContacts.add(contact.getName());
                    }
                    if(myContacts.isEmpty()){
                        alertDialogBox(getString(R.string.contact_alert_dialog_title),getString(R.string.contact_alert_dialog_message) );
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
        Intent intent = new Intent(this, AlarmsActivity.class);
        startActivity(intent);
    }

    public void openNotificationsActivity(){
        if(firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        } else {
            alertDialogBox(getString(R.string.alert_not_logged_title), getString(R.string.alert_not_logged_message));
        }

    }

    public void openSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void checkPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        },PERMISSIONS_CODE);
    }

    //This method guarantees that the files in the /res/raw folder an extracted to the ExternalStorage
    //ready for the app to consume
    //TODO improve code
    private void workOnAdditionalFiles(){
        boolean isCfgHere = false;
        boolean isWeightsHere = false;
        Log.d(TAG, "workOnAdditionalFiles()");

        String path = Objects.requireNonNull(getExternalFilesDir(null)).toString() + "/";

        File directory = new File(path);
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files){
            if (file.getName().equals("yolov3-tiny.cfg")){
                Log.d(TAG, "Configuration file here!");
                isCfgHere = true;
            } else if (file.getName().equals("yolov3-tiny.weights")){
                Log.d(TAG, "Weights file here!");
                isWeightsHere = true;
            }
        }

        if (!isCfgHere){
            File cfgFile = new File(path + "yolov3-tiny.cfg");
            try{
                Log.d(TAG, "isCfgHere try()");
                InputStream inputStream = this.getResources().openRawResource(R.raw.yolov3_tiny_cfg);
                FileOutputStream fileOutputStream = new FileOutputStream(cfgFile);
                byte[] buf = new byte[1024];
                int len;
                while((len=inputStream.read(buf))>0){
                    fileOutputStream.write(buf,0,len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.d(TAG, "isCfgHere catch(): " + e);
                e.printStackTrace();
            }
        }

        if (!isWeightsHere){
            File weightsFile = new File(path + "yolov3-tiny.weights");
            try{
                Log.d(TAG, "isWeightsHere try()");
                InputStream inputStream = this.getResources().openRawResource(R.raw.yolov3_tiny_weights);
                FileOutputStream fileOutputStream = new FileOutputStream(weightsFile);
                byte[] buf = new byte[1024];
                int len;
                while((len=inputStream.read(buf))>0){
                    fileOutputStream.write(buf,0,len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.d(TAG, "isWeightsHere catch(): " + e);
                e.printStackTrace();
            }
        }
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
