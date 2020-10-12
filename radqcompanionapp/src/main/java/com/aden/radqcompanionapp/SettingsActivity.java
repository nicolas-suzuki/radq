package com.aden.radqcompanionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {
    private Button btMyAccount;

    private Snackbar mySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        btMyAccount = findViewById(R.id.btMyAccount);

        //mySnackbar = Snackbar.make(findViewById(R.id.clSettings), R.string.settings_saved, Snackbar.LENGTH_SHORT)
        //        .setBackgroundTint(getResources().getColor(R.color.colorPrimaryDark));

        btMyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, MyAccountActivity.class);
        startActivity(intent);
    }
}
