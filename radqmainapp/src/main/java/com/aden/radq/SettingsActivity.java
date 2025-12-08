package com.aden.radq;

import static com.aden.radq.utils.NotificationView.showSnackbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.aden.radq.utils.FirebaseConnector;
import com.aden.radq.utils.SettingsStorage;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    //Views
    private SwitchCompat swCameraFrontBack;
    private SwitchCompat swRobotInstructions;
    private View thisRootView;

    private SettingsStorage settingsStorage;

    //Firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //Initialize Views
        Button btMyAccount = findViewById(R.id.btMyAccount);
        Button btMyContacts = findViewById(R.id.btMyContacts);
        TextView tvRadqVersion = findViewById(R.id.tvRadqVersion);
        swCameraFrontBack = findViewById(R.id.swCameraFrontBack);
        swRobotInstructions = findViewById(R.id.swRobotInstructions);
        thisRootView = findViewById(R.id.clSettingsActivity);

        //Get RADQ Version
        String radqVersion = getString(R.string.version) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        tvRadqVersion.setText(radqVersion);

        //Load settings
        settingsStorage = new SettingsStorage(SettingsActivity.this);

        //Initialize Firebase
        firebaseAuth = FirebaseConnector.getFirebaseAuth();

        btMyContacts.setOnClickListener(v -> {
            if(firebaseAuth.getCurrentUser() != null){
                if(settingsStorage.getIdentifierKey().isEmpty()){
                    showSnackbar(thisRootView, getString(R.string.not_logged_in));
                } else {
                    openMyContactsActivity();
                }
            } else {
                showSnackbar(thisRootView, getString(R.string.not_logged_in));
            }
        });

        btMyAccount.setOnClickListener(v -> openMyAccountActivity());

        swCameraFrontBack.setOnClickListener(v -> settingsStorage.setSwitchCameraFrontBack(swCameraFrontBack.isChecked()));
        swRobotInstructions.setOnClickListener(v -> settingsStorage.setRobotInstructions(swRobotInstructions.isChecked()));

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
        swCameraFrontBack.setChecked(settingsStorage.getSwitchCameraFrontBack());
        swRobotInstructions.setChecked(settingsStorage.getRobotInstructions());
    }
}
