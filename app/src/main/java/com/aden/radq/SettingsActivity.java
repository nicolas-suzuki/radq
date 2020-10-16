package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private SwitchCompat swCameraFrontBack;

    private Settings settings;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //Load settings
        settings = new Settings(SettingsActivity.this);
        Log.d("loggedUserID", "loggedUserID in " + TAG + " > "+ settings.getIdentifierKey());

        swCameraFrontBack = findViewById(R.id.swCameraFrontBack);
        Button btSaveSettings = findViewById(R.id.btSaveSettings);
        Button btMyAccount = findViewById(R.id.btMyAccount);
        Button btMyContacts = findViewById(R.id.btMyContacts);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        btMyContacts.setOnClickListener(v -> {
            if(firebaseAuth.getCurrentUser() != null){
                if(settings.getIdentifierKey().isEmpty()){
                    showSnackbar(getString(R.string.not_logged_in));
                } else {
                    openMyContactsActivity();
                }
            } else {
                showSnackbar(getString(R.string.not_logged_in));
            }
        });

        btSaveSettings.setOnClickListener(v -> saveData());

        btMyAccount.setOnClickListener(v -> openMyAccountActivity());

        updateData();
    }

    private void openMyContactsActivity() {
        Log.d(TAG,"openMyContactsActivity()");
        Intent intent = new Intent(this, MyContactsActivity.class);
        startActivity(intent);
    }

    private void openMyAccountActivity() {
        Log.d(TAG,"openMyAccountActivity()");
        Intent intent = new Intent(this, MyAccountActivity.class);
        startActivity(intent);
    }

    public void saveData(){
        Log.d(TAG, "saveData()");
        try{
            settings.setSwitchCameraFrontBack(swCameraFrontBack.isChecked());
            showSnackbar(getString(R.string.settings_saved));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateData(){
        Log.d(TAG, "updateData()");
        swCameraFrontBack.setChecked(settings.getSwitchCameraFrontBack());
    }

    private void showSnackbar(String message){
        Log.d(TAG,"showSnackbar()");
        Snackbar.make(findViewById(R.id.clSettingsActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}
