package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_CAMERA_FRONT_BACK ="switchCameraFrontBack";

    private SwitchCompat swCameraFrontBack;
    private boolean isSwitchBackChecked;

    private Button btSaveSettings;
    private Button btMyAccount;
    private Button btMyContacts;

    private Snackbar mySnackbar;

    private Settings settings;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        settings = new Settings(SettingsActivity.this);

        swCameraFrontBack = findViewById(R.id.swCameraFrontBack);
        btSaveSettings = findViewById(R.id.btSaveSettings);
        btMyAccount = findViewById(R.id.btMyAccount);
        btMyContacts = findViewById(R.id.btMyContacts);

        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        mySnackbar = Snackbar.make(findViewById(R.id.clSettings), R.string.settings_saved, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        btMyContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth.getCurrentUser() != null){
                    if(settings.getIdentifierKey().isEmpty()){
                        Snackbar.make(findViewById(R.id.clSettings), R.string.not_logged_in, Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                    } else {
                        openMyContactsActivity();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.clSettings), R.string.not_logged_in, Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
                }
            }
        });

        btSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        btMyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMyAccountActivity();
            }
        });

        loadData();
        updateData();
    }

    private void openMyContactsActivity() {
        Intent intent = new Intent(this, MyContactsActivity.class);
        startActivity(intent);
    }

    private void openMyAccountActivity() {
        Intent intent = new Intent(this, MyAccountActivity.class);
        startActivity(intent);
    }

    public void saveData(){
        Log.d(TAG, "saveData()");
        try{
            settings.setSwitchCameraFrontBack(swCameraFrontBack.isChecked());
            mySnackbar.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadData(){
        Log.d(TAG, "loadData()");
        isSwitchBackChecked = settings.getSwitchCameraFrontBack();
    }

    public void updateData(){
        Log.d(TAG, "updateData()");
        swCameraFrontBack.setChecked(settings.getSwitchCameraFrontBack());
    }
}
