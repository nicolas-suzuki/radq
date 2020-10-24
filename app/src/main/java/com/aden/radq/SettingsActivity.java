package com.aden.radq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.aden.radq.adapter.FirebaseConnector;
import com.aden.radq.helper.Settings;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    //Views
    private SwitchCompat swCameraFrontBack;
    private SwitchCompat swRobotInstructions;

    private Settings settings;

    //Firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //Initialize Views
        Button btMyAccount = findViewById(R.id.btMyAccount);
        Button btMyContacts = findViewById(R.id.btMyContacts);
        swCameraFrontBack = findViewById(R.id.swCameraFrontBack);
        swRobotInstructions = findViewById(R.id.swRobotInstructions);

        //Load settings
        settings = new Settings(SettingsActivity.this);

        //Initialize Firebase
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

        btMyAccount.setOnClickListener(v -> openMyAccountActivity());

        swCameraFrontBack.setOnClickListener(v -> settings.setSwitchCameraFrontBack(swCameraFrontBack.isChecked()));
        swRobotInstructions.setOnClickListener(v -> settings.setRobotInstructions(swRobotInstructions.isChecked()));

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

    public final void updateData(){
        swCameraFrontBack.setChecked(settings.getSwitchCameraFrontBack());
        swRobotInstructions.setChecked(settings.getRobotInstructions());
    }

    private void showSnackbar(String message){
        Snackbar.make(findViewById(R.id.clSettingsActivity), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark)).show();
    }
}
