package com.aden.radq;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

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

        bttnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        loadData();
        updateData();
    }

    public void saveData(){
        Log.i("settingsData", "saveData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, switchCameraFrontBack.isChecked());
        editor.apply();
    }

    public void loadData(){
        Log.i("settingsData", "loadData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        isSwitchBackChecked = sharedPreferences.getBoolean(SWITCH_CAMERA_FRONT_BACK,false);
    }

    public void updateData(){
        Log.i("settingsData", "updateData()");
        switchCameraFrontBack.setChecked(isSwitchBackChecked);
    }
}
