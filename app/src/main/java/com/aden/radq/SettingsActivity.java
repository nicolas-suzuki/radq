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
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_CAMERA_FRONT_BACK ="switchCameraFrontBack";

    private SwitchCompat switchCameraFrontBack;
    private boolean isSwitchBackChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        switchCameraFrontBack = findViewById(R.id.switchCameraFrontBack);
        Button bttnSaveSettings = findViewById(R.id.bttnSaveSettings);
        Button bttnDefineSafeContact = findViewById(R.id.bttnDefineSafeContact);
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.settingsView), R.string.settings_saved, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        bttnDefineSafeContact.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openContactsActivity();
            }
        });

        bttnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySnackbar.show();
                saveData();
            }
        });

        loadData();
        updateData();
    }

    public void openContactsActivity(){
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    public void saveData(){
        Log.d("settingsData", "saveData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, switchCameraFrontBack.isChecked());
        editor.apply();
    }

    public void loadData(){
        Log.d("settingsData", "loadData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        isSwitchBackChecked = sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK,false);
    }

    public void updateData(){
        Log.d("settingsData", "updateData()");
        switchCameraFrontBack.setChecked(isSwitchBackChecked);
    }
}
