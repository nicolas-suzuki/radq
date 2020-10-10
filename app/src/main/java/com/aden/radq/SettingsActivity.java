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

    private SwitchCompat switchCameraFrontBack;
    private boolean isSwitchBackChecked;

    Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        switchCameraFrontBack = findViewById(R.id.switchCameraFrontBack);
        Button bttnSaveSettings = findViewById(R.id.bttnSaveSettings);
        Button buttonContactLogin = findViewById(R.id.btnLoginLogout);

        mySnackbar = Snackbar.make(findViewById(R.id.settingsView), R.string.settings_saved, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        bttnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        buttonContactLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openContactLoginActivity();
            }

        });

        loadData();
        updateData();
    }

    private void openContactLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void saveData(){
        Log.d(TAG, "saveData()");
        try{
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SWITCH_CAMERA_FRONT_BACK, switchCameraFrontBack.isChecked());
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
        switchCameraFrontBack.setChecked(isSwitchBackChecked);
    }
}
