package com.aden.radq;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_CAMERA_FRONT_BACK ="switchCameraFrontBack";
    public static final String IDENTIFIER_KEY = "identifierLoggedUser";

    private SwitchCompat swCameraFrontBack;
    private boolean isSwitchBackChecked;

    Button btSaveSettings;
    Button btLoginLogout;
    Button buttonAddContact;

    Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        swCameraFrontBack = findViewById(R.id.swCameraFrontBack);
        btSaveSettings = findViewById(R.id.btSaveSettings);
        btLoginLogout = findViewById(R.id.btLoginLogout);
        buttonAddContact = findViewById(R.id.btAddContact);

        mySnackbar = Snackbar.make(findViewById(R.id.clSettings), R.string.settings_saved, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        buttonAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactActivity();
            }
        });

        btSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        btLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        loadData();
        updateData();
    }

    private void addContactActivity() {
        Intent intent = new Intent(this, AddContactActivity.class);
        startActivity(intent);
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void saveData(){
        Log.d(TAG, "saveData()");
        try{
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, swCameraFrontBack.isChecked());
            editor.apply();
            mySnackbar.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadData(){
        Log.d(TAG, "loadData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        isSwitchBackChecked = sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK,false);
    }

    public void updateData(){
        Log.d(TAG, "updateData()");
        swCameraFrontBack.setChecked(isSwitchBackChecked);
    }
}
