package com.aden.radq;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import static com.aden.radq.SettingsActivity.SHARED_PREFS;

public class ContactActivity extends AppCompatActivity {
    public static final String CONTACT_NAME = "contactName";
    EditText contactName;
    String contactNameString;

    public static final String CONTACT_EMAIL = "contactEmail";
    EditText contactEmail;
    String contactEmailString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_activity);

        contactName = findViewById(R.id.contactNameEditTxt);
        contactEmail = findViewById(R.id.contactEmailEditTxt);
        Button bttnSaveSettings = findViewById(R.id.bttnSaveContact);

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
        editor.putString(CONTACT_NAME,contactName.getText().toString());
        editor.putString(CONTACT_EMAIL,contactEmail.getText().toString());
        //TODO validations

        editor.apply();
    }

    public void loadData(){
        Log.i("settingsData", "loadData()");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        contactEmailString = sharedPreferences.getString(CONTACT_EMAIL,"");
        contactNameString = sharedPreferences.getString(CONTACT_NAME,"");
    }

    public void updateData(){
        Log.i("settingsData", "updateData()");
        contactName.setText(contactNameString);
        contactEmail.setText(contactEmailString);
    }
}
